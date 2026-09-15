package com.almara.modules.geo.services;

import com.almara.modules.geo.models.CoordenadasGps;
import java.util.List;

/**
 * Puerto del patrón Adapter para desacoplar el motor espacial H3 Core
 * del resto de la arquitectura de la aplicación Almara.
 */
public interface AdaptadorH3 {

    /**
     * Convierte coordenadas geográficas precisas a una celda hexagonal H3
     * en la resolución espacial indicada.
     */
    String coordenadasACelda(double latitud, double longitud, int resolucion);

    /**
     * Obtiene las coordenadas del centroide de una celda H3 para renderizado rápido.
     */
    CoordenadasGps celdaACentroide(String idCeldaH3);

    /**
     * Valida si un identificador corresponde a una celda H3 sintácticamente válida.
     */
    boolean esCeldaValida(String idCeldaH3);

    /**
     * Obtiene la resolución espacial de una celda H3 dada.
     */
    int obtenerResolucion(String idCeldaH3);

    /**
     * Obtiene la celda padre de menor resolución espacial (Strategy Pattern para zoom del mapa, HU-05).
     */
    String obtenerCeldaPadre(String idCeldaH3, int resolucionPadre);

    /**
     * Obtiene los vértices geográficos del polígono hexagonal de la celda para su dibujo en el mapa interactivo (HU-05).
     */
    List<CoordenadasGps> obtenerLimitesHexagono(String idCeldaH3);
}
