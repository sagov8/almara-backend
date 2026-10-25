package com.almara.modules.admin.patterns.adapter;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.services.AdaptadorH3;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación concreta del Adaptador Espacial (HU-14).
 * Conecta las librerías espaciales Uber H3 Core con las especificaciones PostGIS / OGC GeoJSON.
 */
@Component
public class AdaptadorEspacialPostgisH3Impl implements AdaptadorEspacialPostgisH3 {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorEspacialPostgisH3Impl.class);
    private static final double EPSILON_CIERRE = 0.000001;

    private final AdaptadorH3 adaptadorH3;
    private final ObjectMapper objectMapper;

    public AdaptadorEspacialPostgisH3Impl(AdaptadorH3 adaptadorH3, ObjectMapper objectMapper) {
        this.adaptadorH3 = adaptadorH3;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean validarGeoJsonPoligonoCerrado(String geoJson) {
        if (geoJson == null || geoJson.trim().isEmpty()) {
            return false;
        }

        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode coordinatesNode = resolverNodoCoordenadas(root);

            if (coordinatesNode == null || !coordinatesNode.isArray() || coordinatesNode.isEmpty()) {
                return false;
            }

            JsonNode linearRing = coordinatesNode.get(0);
            if (!linearRing.isArray() || linearRing.size() < 4) {
                // Un polígono cerrado conforme a RFC 7946 requiere al menos 4 puntos (primer punto == último punto)
                return false;
            }

            JsonNode primerPunto = linearRing.get(0);
            JsonNode ultimoPunto = linearRing.get(linearRing.size() - 1);

            double lonInicio = primerPunto.get(0).asDouble();
            double latInicio = primerPunto.get(1).asDouble();
            double lonFin = ultimoPunto.get(0).asDouble();
            double latFin = ultimoPunto.get(1).asDouble();

            boolean cerrado = Math.abs(lonInicio - lonFin) < EPSILON_CIERRE &&
                              Math.abs(latInicio - latFin) < EPSILON_CIERRE;

            if (!cerrado) {
                log.warn("El polígono GeoJSON no está cerrado: inicio=({}, {}), fin=({}, {})",
                        lonInicio, latInicio, lonFin, latFin);
            }

            return cerrado;
        } catch (Exception e) {
            log.warn("Error sintáctico al analizar GeoJSON: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<CoordenadasGps> extraerVerticesPoligono(String geoJson) {
        if (!validarGeoJsonPoligonoCerrado(geoJson)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "GEOJSON_INVALIDO",
                    "La geometría no corresponde a un polígono GeoJSON cerrado válido (RFC 7946)."
            );
        }

        try {
            JsonNode root = objectMapper.readTree(geoJson);
            JsonNode linearRing = resolverNodoCoordenadas(root).get(0);

            List<CoordenadasGps> vertices = new ArrayList<>();
            for (JsonNode punto : linearRing) {
                double lon = punto.get(0).asDouble();
                double lat = punto.get(1).asDouble();
                vertices.add(new CoordenadasGps(lat, lon));
            }
            return vertices;
        } catch (Exception e) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "ERROR_PARSEO_GEOJSON",
                    "Error al extraer coordenadas del polígono: " + e.getMessage()
            );
        }
    }

    @Override
    public CoordenadasGps calcularCentroide(String geoJson) {
        List<CoordenadasGps> vertices = extraerVerticesPoligono(geoJson);
        if (vertices.isEmpty()) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "POLIGONO_SIN_VERTICES",
                    "El polígono no contiene vértices suficientes para calcular su centroide."
            );
        }

        double sumaLat = 0.0;
        double sumaLon = 0.0;
        int total = vertices.size() - 1; // Descontar el punto de cierre duplicado

        for (int i = 0; i < total; i++) {
            sumaLat += vertices.get(i).getLatitud();
            sumaLon += vertices.get(i).getLongitud();
        }

        // Redondeo a 6 decimales (~0.1 metros de precisión GPS OGC estándar)
        double latRedondeada = Math.round((sumaLat / total) * 1_000_000.0) / 1_000_000.0;
        double lonRedondeada = Math.round((sumaLon / total) * 1_000_000.0) / 1_000_000.0;

        return new CoordenadasGps(latRedondeada, lonRedondeada);
    }

    @Override
    public String centroideACeldaH3(String geoJson, int resolucionH3) {
        CoordenadasGps centroide = calcularCentroide(geoJson);
        return adaptadorH3.coordenadasACelda(centroide.getLatitud(), centroide.getLongitud(), resolucionH3);
    }

    @Override
    public int estimarCeldasCubiertas(String geoJson, int resolucionH3) {
        List<CoordenadasGps> vertices = extraerVerticesPoligono(geoJson);
        // Aproximación del área basada en bounding box simple para resolución H3 dada
        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (CoordenadasGps c : vertices) {
            minLat = Math.min(minLat, c.getLatitud());
            maxLat = Math.max(maxLat, c.getLatitud());
            minLon = Math.min(minLon, c.getLongitud());
            maxLon = Math.max(maxLon, c.getLongitud());
        }

        double diffLat = Math.max(0.001, maxLat - minLat);
        double diffLon = Math.max(0.001, maxLon - minLon);

        // Área en km² aproximada en latitud 2.4° (Popayán)
        double areaAproxKm2 = (diffLat * 111.0) * (diffLon * 111.0 * Math.cos(Math.toRadians(2.44)));

        // Área promedio por celda según resolución H3:
        // Res 7: ~5.16 km², Res 8: ~0.73 km², Res 9: ~0.10 km², Res 10: ~0.015 km²
        double areaCeldaKm2 = switch (resolucionH3) {
            case 7 -> 5.16;
            case 8 -> 0.73;
            case 10 -> 0.015;
            default -> 0.105; // Resolución 9 por defecto
        };

        return Math.max(1, (int) Math.round(areaAproxKm2 / areaCeldaKm2));
    }

    @Override
    public String generarExpresionSqlPostgis(String geoJson) {
        // Expresión SQL OGC nativa para PostGIS con SRID 4326 (WGS 84)
        return "ST_SetSRID(ST_GeomFromGeoJSON('" + geoJson.replace("'", "''") + "'), 4326)";
    }

    private JsonNode resolverNodoCoordenadas(JsonNode root) {
        if (root.has("type") && "Feature".equalsIgnoreCase(root.get("type").asText())) {
            JsonNode geometry = root.get("geometry");
            return geometry != null ? geometry.get("coordinates") : null;
        }
        if (root.has("type") && "Polygon".equalsIgnoreCase(root.get("type").asText())) {
            return root.get("coordinates");
        }
        if (root.has("coordinates")) {
            return root.get("coordinates");
        }
        return null;
    }
}
