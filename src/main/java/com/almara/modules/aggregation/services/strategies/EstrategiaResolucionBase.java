package com.almara.modules.aggregation.services.strategies;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.models.ContextoZoomMapa;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.services.AdaptadorH3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase base con la lógica común de agregación espacial, cálculo de emoción predominante
 * y aplicación de Protección en Comunidad (k >= 5) con explicaciones accesibles.
 */
public abstract class EstrategiaResolucionBase implements EstrategiaResolucionEspacial {

    @Override
    public List<CeldaMapaEmocional> agregarCeldas(List<ReporteEmocion> reportes,
                                                  ContextoZoomMapa contexto,
                                                  Map<String, Zona> mapaZonas,
                                                  AdaptadorH3 adaptadorH3) {
        if (reportes == null || reportes.isEmpty()) {
            return List.of();
        }

        int resolucionObjetivo = getResolucionH3();

        // Agrupar los reportes por la celda H3 a la resolución de esta estrategia
        Map<String, List<ReporteEmocion>> reportesPorCelda = new HashMap<>();

        for (ReporteEmocion reporte : reportes) {
            String celdaOriginal = reporte.getIdCeldaH3();
            if (celdaOriginal == null || !adaptadorH3.esCeldaValida(celdaOriginal)) {
                continue;
            }

            String celdaAgregada = adaptadorH3.obtenerCeldaPadre(celdaOriginal, resolucionObjetivo);
            reportesPorCelda.computeIfAbsent(celdaAgregada, k -> new ArrayList<>()).add(reporte);
        }

        List<CeldaMapaEmocional> resultado = new ArrayList<>();
        int umbralK = contexto != null ? contexto.getUmbralMinimoK() : 5;

        for (Map.Entry<String, List<ReporteEmocion>> entry : reportesPorCelda.entrySet()) {
            String idCelda = entry.getKey();
            List<ReporteEmocion> reportesEnCelda = entry.getValue();
            int totalReportes = reportesEnCelda.size();

            CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCelda);
            if (centroide == null) {
                continue;
            }

            // Filtrado por límites geográficos si el contexto lo especifica
            if (contexto != null && contexto.tieneFiltroGeografico()) {
                double lat = centroide.getLatitud();
                double lon = centroide.getLongitud();
                if (lat < contexto.getMinLat() || lat > contexto.getMaxLat()
                        || lon < contexto.getMinLon() || lon > contexto.getMaxLon()) {
                    continue;
                }
            }

            // Identificar nombre de zona representativa
            String nombreZona = resolverNombreZona(idCelda, reportesEnCelda, mapaZonas);
            List<CoordenadasGps> vertices = adaptadorH3.obtenerLimitesHexagono(idCelda);

            boolean cumpleUmbral = totalReportes >= umbralK;

            if (cumpleUmbral) {
                TipoEmocion emocionPredominante = calcularEmocionPredominante(reportesEnCelda);
                String icono = resolverIcono(emocionPredominante);

                // Explicación amigable orientada a ciudadanos, sin tecnicismos
                String descProteccion = "Protección en Comunidad activa: " + totalReportes
                        + " vecinos han compartido su sentir aquí de forma anónima.";

                CeldaMapaEmocional celda = CeldaMapaEmocional.builder()
                        .idCeldaH3(idCelda)
                        .resolucionH3(resolucionObjetivo)
                        .latitudCentroide(centroide.getLatitud())
                        .longitudCentroide(centroide.getLongitud())
                        .verticesHexagono(vertices)
                        .emocionPredominante(emocionPredominante)
                        .nombreEmocion(emocionPredominante.getEtiquetaVisible())
                        .codigoHexColor(emocionPredominante.getCodigoHexColor())
                        .codigoHexFondo(emocionPredominante.getCodigoHexFondo())
                        .icono(icono)
                        .totalReportes(totalReportes)
                        .cumpleUmbral(true)
                        .nombreZona(nombreZona)
                        .descripcionProteccion(descProteccion)
                        .build();

                resultado.add(celda);
            } else {
                // RNF Seguridad: Las celdas que no alcanzan el umbral mínimo no deben revelar la emoción individual
                String descProteccion = "Zona en reserva: Se necesitan al menos " + umbralK
                        + " opiniones para mostrar el ánimo colectivo (van " + totalReportes + "). Tu privacidad está protegida.";

                CeldaMapaEmocional celdaProtegida = CeldaMapaEmocional.builder()
                        .idCeldaH3(idCelda)
                        .resolucionH3(resolucionObjetivo)
                        .latitudCentroide(centroide.getLatitud())
                        .longitudCentroide(centroide.getLongitud())
                        .verticesHexagono(vertices)
                        .emocionPredominante(null)
                        .nombreEmocion("Protegida")
                        .codigoHexColor("#94A3B8")
                        .codigoHexFondo("#F1F5F9")
                        .icono("🔒")
                        .totalReportes(totalReportes)
                        .cumpleUmbral(false)
                        .nombreZona(nombreZona)
                        .descripcionProteccion(descProteccion)
                        .build();

                resultado.add(celdaProtegida);
            }
        }

        return resultado;
    }

    protected TipoEmocion calcularEmocionPredominante(List<ReporteEmocion> reportes) {
        Map<TipoEmocion, Long> conteo = reportes.stream()
                .filter(r -> r.getEmocion() != null)
                .collect(Collectors.groupingBy(ReporteEmocion::getEmocion, Collectors.counting()));

        return conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TipoEmocion.NEUTRALIDAD);
    }

    protected String resolverNombreZona(String idCeldaH3, List<ReporteEmocion> reportes, Map<String, Zona> mapaZonas) {
        if (mapaZonas != null && mapaZonas.containsKey(idCeldaH3)) {
            return mapaZonas.get(idCeldaH3).getNombreZonaManual();
        }

        // Si algún reporte dentro de esta celda padre tenía zona asociada
        for (ReporteEmocion r : reportes) {
            if (r.getIdCeldaH3() != null && mapaZonas != null && mapaZonas.containsKey(r.getIdCeldaH3())) {
                return mapaZonas.get(r.getIdCeldaH3()).getNombreZonaManual();
            }
        }

        return "Sector " + idCeldaH3.substring(0, Math.min(idCeldaH3.length(), 8));
    }

    protected String resolverIcono(TipoEmocion emocion) {
        if (emocion == null) return "📍";
        return switch (emocion) {
            case FELICIDAD -> "😊";
            case NEUTRALIDAD -> "😐";
            case PREOCUPACION -> "😟";
            case ENFADO -> "😡";
            case ANSIEDAD -> "😰";
        };
    }
}
