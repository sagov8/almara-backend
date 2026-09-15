package com.almara.modules.aggregation.services;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.models.ConsultaMapaEmocionalRespuesta;
import com.almara.modules.aggregation.models.ContextoZoomMapa;
import com.almara.modules.aggregation.services.strategies.EstrategiaResolucionEspacial;
import com.almara.modules.aggregation.services.strategies.EstrategiaZoomDetalle;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import com.almara.modules.geo.services.AdaptadorH3;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de agregación y consulta para el mapa interactivo colectivo (HU-05).
 * Implementa el patrón Strategy para alternar dinámicamente la resolución H3 según el zoom,
 * y aplica el principio de Protección en Comunidad (mínimo 5 personas) con lenguaje amigable.
 */
@Service
public class ServicioAgregacionMapa {

    private static final Logger log = LoggerFactory.getLogger(ServicioAgregacionMapa.class);
    public static final String MENSAJE_PROTECCION_DEFAULT =
            "Protección en Comunidad: Para cuidar tu privacidad, las zonas solo se colorean " +
            "en el mapa cuando al menos 5 vecinos han compartido cómo se sienten en ese lugar. " +
            "Tu participación es 100% anónima.";

    private final List<EstrategiaResolucionEspacial> estrategias;
    private final AdaptadorH3 adaptadorH3;
    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final RepositorioZona repositorioZona;

    public ServicioAgregacionMapa(List<EstrategiaResolucionEspacial> estrategias,
                                  AdaptadorH3 adaptadorH3,
                                  RepositorioReporteEmocion repositorioReporteEmocion,
                                  RepositorioZona repositorioZona) {
        this.estrategias = estrategias;
        this.adaptadorH3 = adaptadorH3;
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.repositorioZona = repositorioZona;
    }

    @PostConstruct
    public void inicializarDatosDemostracionPopayan() {
        if (repositorioReporteEmocion.contarTotal() == 0) {
            log.info("Sembrando reportes colectivos iniciales para Popayán (HU-05)...");
            sembrarReportesIniciales();
        }
    }

    /**
     * Consulta las celdas emocionales visibles según el nivel de zoom y área del mapa.
     * Cumple con HU-05 Validaciones: Únicamente muestra celdas que alcancen el umbral de protección.
     */
    public ConsultaMapaEmocionalRespuesta consultarCeldas(ContextoZoomMapa contexto) {
        long inicioMs = System.currentTimeMillis();

        if (contexto == null) {
            contexto = ContextoZoomMapa.builder().nivelZoom(14).umbralMinimoK(5).build();
        }

        EstrategiaResolucionEspacial estrategia = seleccionarEstrategia(contexto.getNivelZoom());
        log.debug("Estrategia espacial seleccionada: {} (Resolución {})",
                estrategia.getNombreEstrategia(), estrategia.getResolucionH3());

        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();

        // Mapa de consulta rápida de zonas por idCeldaH3
        Map<String, Zona> mapaZonas = repositorioZona.obtenerZonasManuales().stream()
                .collect(Collectors.toMap(Zona::getIdCeldaH3, Function.identity(), (z1, z2) -> z1));

        // Ejecución de la estrategia
        List<CeldaMapaEmocional> celdasCalculadas = estrategia.agregarCeldas(
                todosReportes, contexto, mapaZonas, adaptadorH3);

        // HU-05 Criterio 2 y 3: Únicamente se muestran las zonas que cumplen con el umbral mínimo
        List<CeldaMapaEmocional> celdasVisibles = celdasCalculadas.stream()
                .filter(CeldaMapaEmocional::isCumpleUmbral)
                .toList();

        long duracionMs = System.currentTimeMillis() - inicioMs;

        return ConsultaMapaEmocionalRespuesta.builder()
                .celdasVisibles(celdasVisibles)
                .totalCeldasVisibles(celdasVisibles.size())
                .totalReportesEnMapa(todosReportes.size())
                .umbralMinimoAplicado(contexto.getUmbralMinimoK())
                .resolucionH3Utilizada(estrategia.getResolucionH3())
                .mensajeProteccionComunidad(MENSAJE_PROTECCION_DEFAULT)
                .tiempoCalculoMs(duracionMs)
                .build();
    }

    /**
     * Retorna todas las celdas (tanto visibles como protegidas) para visualización
     * de auditoría o modo exploración con indicador de reserva ciudadana.
     */
    public List<CeldaMapaEmocional> obtenerTodasLasCeldas(ContextoZoomMapa contexto) {
        if (contexto == null) {
            contexto = ContextoZoomMapa.builder().nivelZoom(14).umbralMinimoK(5).build();
        }
        EstrategiaResolucionEspacial estrategia = seleccionarEstrategia(contexto.getNivelZoom());
        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();
        Map<String, Zona> mapaZonas = repositorioZona.obtenerZonasManuales().stream()
                .collect(Collectors.toMap(Zona::getIdCeldaH3, Function.identity(), (z1, z2) -> z1));

        return estrategia.agregarCeldas(todosReportes, contexto, mapaZonas, adaptadorH3);
    }

    /**
     * Selecciona la estrategia adecuada según el nivel de zoom especificado.
     */
    public EstrategiaResolucionEspacial seleccionarEstrategia(int nivelZoom) {
        return estrategias.stream()
                .filter(e -> e.soportaZoom(nivelZoom))
                .findFirst()
                .orElseGet(() -> estrategias.stream()
                        .filter(e -> e instanceof EstrategiaZoomDetalle)
                        .findFirst()
                        .orElse(estrategias.get(0)));
    }

    /**
     * Siembra reportes iniciales para las zonas emblemáticas de Popayán.
     * Incluye zonas con suficientes reportes (cumplen umbral) y una zona con pocos reportes
     * para verificar la protección ciudadana en el mapa.
     */
    public void sembrarReportesIniciales() {
        List<Zona> zonas = repositorioZona.obtenerZonasManuales();
        if (zonas.isEmpty()) return;

        Map<String, TipoEmocion> emocionesPorManualId = Map.ofEntries(
                Map.entry("ZONA-PARQUE-CALDAS", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-HUMILLADERO", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-MORRO-TULCAN", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-ERMITA", TipoEmocion.NEUTRALIDAD),
                Map.entry("ZONA-TERRA-PLAZA", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-CAMPUS-TULCAN", TipoEmocion.NEUTRALIDAD),
                Map.entry("ZONA-BELLA-VISTA", TipoEmocion.NEUTRALIDAD),
                Map.entry("ZONA-VILLA-OLIMPICA", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-RINCON-PAYANES", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-YANACONAS", TipoEmocion.NEUTRALIDAD),
                Map.entry("ZONA-PARQUE-SALUD", TipoEmocion.FELICIDAD),
                Map.entry("ZONA-LA-ESMERALDA", TipoEmocion.PREOCUPACION),
                Map.entry("ZONA-EL-EMPEDRADO", TipoEmocion.ANSIEDAD),
                Map.entry("ZONA-CAMPANARIO", TipoEmocion.NEUTRALIDAD)
        );

        for (Zona zona : zonas) {
            String manualId = zona.getZonaManualId();
            if (manualId == null) continue;

            // Panteón de los próceres tendrá solo 2 reportes para validar zona protegida
            if ("ZONA-PANTEON-PROCERES".equalsIgnoreCase(manualId)) {
                for (int i = 0; i < 2; i++) {
                    ReporteEmocion r = ReporteEmocion.builder()
                            .idEvento(UUID.randomUUID())
                            .emocion(TipoEmocion.NEUTRALIDAD)
                            .intensidad(3.0f)
                            .idCeldaH3(zona.getIdCeldaH3())
                            .fechaHora(Instant.now().minusSeconds(600L * i))
                            .tokenSesionTemporal(UUID.randomUUID().toString())
                            .build();
                    repositorioReporteEmocion.guardar(r);
                }
                continue;
            }

            TipoEmocion emocionDominante = emocionesPorManualId.getOrDefault(manualId, TipoEmocion.FELICIDAD);
            // Generar entre 6 y 10 reportes para que cumplan con holgura el umbral k >= 5
            int cantidad = 6 + (Math.abs(manualId.hashCode()) % 5);

            for (int i = 0; i < cantidad; i++) {
                // Mayoría con la emoción dominante, alguno con otra emoción
                TipoEmocion em = (i < cantidad - 1) ? emocionDominante : TipoEmocion.NEUTRALIDAD;
                ReporteEmocion r = ReporteEmocion.builder()
                        .idEvento(UUID.randomUUID())
                        .emocion(em)
                        .intensidad((float) (3 + (i % 3)))
                        .idCeldaH3(zona.getIdCeldaH3())
                        .fechaHora(Instant.now().minusSeconds(300L * i))
                        .tokenSesionTemporal(UUID.randomUUID().toString())
                        .build();
                repositorioReporteEmocion.guardar(r);
            }
        }
    }
}
