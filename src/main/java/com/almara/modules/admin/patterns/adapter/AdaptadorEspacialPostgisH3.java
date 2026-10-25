package com.almara.modules.admin.patterns.adapter;

import com.almara.modules.geo.models.CoordenadasGps;

import java.util.List;

/**
 * Patrón Adapter (HU-14): Puerto adaptador para la interacción entre el motor
 * geoespacial Uber H3 y la Base de Datos Espacial (PostGIS / OGC GeoJSON).
 */
public interface AdaptadorEspacialPostgisH3 {

    /**
     * Valida que la cadena represente una geometría GeoJSON Polygon válida,
     * cerrada y compatible con los estándares OGC y RFC 7946.
     *
     * @param geoJson Cadena con la estructura GeoJSON
     * @return true si es un polígono cerrado válido
     */
    boolean validarGeoJsonPoligonoCerrado(String geoJson);

    /**
     * Extrae las coordenadas del polígono GeoJSON como pares (longitud, latitud).
     */
    List<CoordenadasGps> extraerVerticesPoligono(String geoJson);

    /**
     * Calcula el centroide geográfico del polígono para indexación y renderizado.
     */
    CoordenadasGps calcularCentroide(String geoJson);

    /**
     * Mapea el centroide del polígono a un identificador de celda Uber H3
     * en la resolución espacial requerida.
     */
    String centroideACeldaH3(String geoJson, int resolucionH3);

    /**
     * Estima la cantidad de celdas H3 que recubren el polígono espacial.
     */
    int estimarCeldasCubiertas(String geoJson, int resolucionH3);

    /**
     * Genera la expresión SQL OGC nativa de PostGIS para almacenamiento geométrico (SRID 4326).
     */
    String generarExpresionSqlPostgis(String geoJson);
}
