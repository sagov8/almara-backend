package com.almara.modules.aggregation.services;

import com.almara.modules.aggregation.models.DetalleZonaEmocionRespuesta;
import com.almara.modules.aggregation.models.PeriodoTemporal;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import com.almara.modules.geo.services.AdaptadorH3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Servicio de agregación y detalle para zonas emocionales (HU-06).
 * Implementa cálculo de porcentajes exactos (suman 100%), aproximación de participantes
 * para privacidad estricta y caché en memoria para respuesta < 300 ms.
 */
@Service
public class ServicioDetalleZona {

    private static final Logger log = LoggerFactory.getLogger(ServicioDetalleZona.class);
    public static final int UMBRAL_MINIMO_DETALLE = 5;

    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final RepositorioZona repositorioZona;
    private final AdaptadorH3 adaptadorH3;

    // Caché en memoria para optimizar tiempo de respuesta (RNF Rendimiento < 300 ms)
    private final Map<String, CacheEntrada> cacheDetalles = new ConcurrentHashMap<>();
    private static final long TIEMPO_VIDA_CACHE_MS = 15_000; // 15 segundos

    private record CacheEntrada(DetalleZonaEmocionRespuesta respuesta, long timestamp) {}

    public ServicioDetalleZona(RepositorioReporteEmocion repositorioReporteEmocion,
                               RepositorioZona repositorioZona,
                               AdaptadorH3 adaptadorH3) {
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.repositorioZona = repositorioZona;
        this.adaptadorH3 = adaptadorH3;
    }

    /**
     * Consulta el detalle emocional agregado de una celda seleccionada (HU-06).
     */
    public DetalleZonaEmocionRespuesta consultarDetalleZona(String idCeldaH3, PeriodoTemporal periodo) {
        long inicioMs = System.currentTimeMillis();

        if (periodo == null) {
            periodo = PeriodoTemporal.ULTIMAS_2_HORAS;
        }

        String claveCache = (idCeldaH3 != null ? idCeldaH3.toLowerCase() : "vacio") + ":" + periodo.name();
        CacheEntrada enCache = cacheDetalles.get(claveCache);
        if (enCache != null && (System.currentTimeMillis() - enCache.timestamp()) < TIEMPO_VIDA_CACHE_MS) {
            log.debug("Detalle servido desde caché para celda: {}", idCeldaH3);
            return enCache.respuesta();
        }

        // Obtener reportes de la celda
        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();
        List<ReporteEmocion> reportesCelda = filtrarReportesCelda(todosReportes, idCeldaH3, periodo);

        // Resolver nombre de la zona
        String nombreZona = resolverNombreZona(idCeldaH3);
        int totalReportes = reportesCelda.size();

        DetalleZonaEmocionRespuesta respuesta;

        // Criterio 2 y 3: Si no cumple el umbral mínimo (k < 5), proteger y ocultar emociones individuales
        if (totalReportes < UMBRAL_MINIMO_DETALLE) {
            long duracionMs = System.currentTimeMillis() - inicioMs;
            respuesta = DetalleZonaEmocionRespuesta.builder()
                    .idCeldaH3(idCeldaH3)
                    .nombreZona(nombreZona)
                    .emocionPredominante(null)
                    .nombreEmocion("En reserva ciudadana")
                    .codigoHexColor("#94A3B8")
                    .codigoHexFondo("#F1F5F9")
                    .icono("🔒")
                    .distribucionPorcentajes(Map.of())
                    .tendencia("En espera de más participación ciudadana")
                    .participantesAproximados(calcularParticipantesAproximados(totalReportes))
                    .periodoTemporal(periodo.getEtiqueta())
                    .comentariosRecientes(List.of())
                    .cumpleUmbral(false)
                    .descripcionProteccion("Zona en reserva: Se necesitan al menos " + UMBRAL_MINIMO_DETALLE +
                            " opiniones para desplegar la distribución emocional. Tu privacidad está protegida.")
                    .tiempoCalculoMs(duracionMs)
                    .build();
        } else {
            // Criterio 1: Emoción predominante, porcentajes exactos que suman 100% y tendencia
            TipoEmocion predominante = calcularEmocionPredominante(reportesCelda);
            Map<String, Integer> porcentajes = calcularDistribucionExacta(reportesCelda);
            String tendencia = calcularTendencia(predominante, reportesCelda);
            String participantesAprox = calcularParticipantesAproximados(totalReportes);
            List<String> comentarios = extraerComentariosRecientes(reportesCelda);

            long duracionMs = System.currentTimeMillis() - inicioMs;

            respuesta = DetalleZonaEmocionRespuesta.builder()
                    .idCeldaH3(idCeldaH3)
                    .nombreZona(nombreZona)
                    .emocionPredominante(predominante)
                    .nombreEmocion(predominante.getEtiquetaVisible())
                    .codigoHexColor(predominante.getCodigoHexColor())
                    .codigoHexFondo(predominante.getCodigoHexFondo())
                    .icono(resolverIcono(predominante))
                    .distribucionPorcentajes(porcentajes)
                    .tendencia(tendencia)
                    .participantesAproximados(participantesAprox)
                    .periodoTemporal(periodo.getEtiqueta())
                    .comentariosRecientes(comentarios)
                    .cumpleUmbral(true)
                    .descripcionProteccion("Protección en Comunidad activa: " + participantesAprox +
                            " han compartido su sentir aquí de forma anónima.")
                    .tiempoCalculoMs(duracionMs)
                    .build();
        }

        cacheDetalles.put(claveCache, new CacheEntrada(respuesta, System.currentTimeMillis()));
        return respuesta;
    }

    private List<ReporteEmocion> filtrarReportesCelda(List<ReporteEmocion> reportes, String idCeldaH3, PeriodoTemporal periodo) {
        if (idCeldaH3 == null || reportes == null) return List.of();

        Instant limiteInferior = Instant.now().minus(periodo.getDuracion());

        // Filtrar por celda y por ventana temporal (con fallback inclusivo si es celda padre o datos de prueba)
        List<ReporteEmocion> filtrados = reportes.stream()
                .filter(r -> coincideCelda(r.getIdCeldaH3(), idCeldaH3))
                .filter(r -> r.getFechaHora() == null || r.getFechaHora().isAfter(limiteInferior))
                .toList();

        // Si la ventana temporal excluye todos los reportes pero la celda tiene registros históricos,
        // incluir los reportes de la celda para que la vista siempre sea útil
        if (filtrados.isEmpty()) {
            return reportes.stream()
                    .filter(r -> coincideCelda(r.getIdCeldaH3(), idCeldaH3))
                    .toList();
        }

        return filtrados;
    }

    private boolean coincideCelda(String celdaReporte, String celdaObjetivo) {
        if (celdaReporte == null || celdaObjetivo == null) return false;
        if (celdaReporte.equalsIgnoreCase(celdaObjetivo)) return true;

        if (adaptadorH3.esCeldaValida(celdaReporte) && adaptadorH3.esCeldaValida(celdaObjetivo)) {
            int resObjetivo = adaptadorH3.obtenerResolucion(celdaObjetivo);
            String padre = adaptadorH3.obtenerCeldaPadre(celdaReporte, resObjetivo);
            return celdaObjetivo.equalsIgnoreCase(padre);
        }

        return false;
    }

    /**
     * RNF Completitud: Calcula la distribución porcentual garantizando que la suma sea exactamente 100%.
     * Utiliza el método de los mayores restos (Hamilton-Hare) para evitar pérdidas por redondeo.
     */
    public Map<String, Integer> calcularDistribucionExacta(List<ReporteEmocion> reportes) {
        if (reportes == null || reportes.isEmpty()) {
            return Map.of();
        }

        int total = reportes.size();
        Map<TipoEmocion, Long> conteo = reportes.stream()
                .filter(r -> r.getEmocion() != null)
                .collect(Collectors.groupingBy(ReporteEmocion::getEmocion, Collectors.counting()));

        Map<String, Integer> resultado = new LinkedHashMap<>();
        Map<String, Double> restos = new HashMap<>();
        int sumaPisos = 0;

        for (TipoEmocion em : TipoEmocion.values()) {
            long c = conteo.getOrDefault(em, 0L);
            if (c > 0) {
                double exacto = (c * 100.0) / total;
                int piso = (int) Math.floor(exacto);
                resultado.put(em.name(), piso);
                restos.put(em.name(), exacto - piso);
                sumaPisos += piso;
            }
        }

        // Distribuir los puntos restantes (100 - sumaPisos) a las emociones con mayor residuo
        int puntosFaltantes = 100 - sumaPisos;
        if (puntosFaltantes > 0 && !restos.isEmpty()) {
            List<Map.Entry<String, Double>> ordenados = restos.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .toList();

            for (int i = 0; i < puntosFaltantes; i++) {
                String clave = ordenados.get(i % ordenados.size()).getKey();
                resultado.put(clave, resultado.get(clave) + 1);
            }
        }

        return resultado;
    }

    /**
     * RNF Privacidad (Criterio 3): Oculta el número exacto y presenta rangos aproximados protectores.
     */
    public String calcularParticipantesAproximados(int totalReportes) {
        if (totalReportes < UMBRAL_MINIMO_DETALLE) {
            return "Menos de 5 vecinos";
        }
        if (totalReportes < 10) {
            return "Más de 5 vecinos";
        }
        if (totalReportes < 20) {
            return "Más de 10 vecinos";
        }
        if (totalReportes < 50) {
            return "Más de 20 vecinos";
        }
        return "Más de 50 vecinos";
    }

    private TipoEmocion calcularEmocionPredominante(List<ReporteEmocion> reportes) {
        Map<TipoEmocion, Long> conteo = reportes.stream()
                .filter(r -> r.getEmocion() != null)
                .collect(Collectors.groupingBy(ReporteEmocion::getEmocion, Collectors.counting()));

        return conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TipoEmocion.NEUTRALIDAD);
    }

    private String calcularTendencia(TipoEmocion predominante, List<ReporteEmocion> reportes) {
        if (predominante == null) {
            return "Estable";
        }

        return switch (predominante) {
            case FELICIDAD -> "Clima favorable y positivo en la zona";
            case NEUTRALIDAD -> "Ambiente sereno y equilibrado";
            case PREOCUPACION -> "Clima de atención ciudadana";
            case ENFADO -> "Tensión localizada en seguimiento";
            case ANSIEDAD -> "Ambiente de cautela y dinamismo";
        };
    }

    private List<String> extraerComentariosRecientes(List<ReporteEmocion> reportes) {
        return reportes.stream()
                .map(ReporteEmocion::getComentario)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .limit(5)
                .toList();
    }

    private String resolverNombreZona(String idCeldaH3) {
        Optional<Zona> z = repositorioZona.buscarPorIdCeldaH3(idCeldaH3);
        if (z.isPresent() && z.get().getNombreZonaManual() != null) {
            return z.get().getNombreZonaManual();
        }

        // Buscar entre todas las zonas
        for (Zona zona : repositorioZona.obtenerZonasManuales()) {
            if (coincideCelda(zona.getIdCeldaH3(), idCeldaH3)) {
                return zona.getNombreZonaManual();
            }
        }

        return "Sector " + (idCeldaH3 != null && idCeldaH3.length() >= 8 ? idCeldaH3.substring(0, 8) : idCeldaH3);
    }

    private String resolverIcono(TipoEmocion emocion) {
        if (emocion == null) return "📍";
        return switch (emocion) {
            case FELICIDAD -> "😊";
            case NEUTRALIDAD -> "😐";
            case PREOCUPACION -> "😟";
            case ENFADO -> "😡";
            case ANSIEDAD -> "😰";
        };
    }

    public void limpiarCache() {
        cacheDetalles.clear();
    }
}
