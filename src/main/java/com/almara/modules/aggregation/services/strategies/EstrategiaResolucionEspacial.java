package com.almara.modules.aggregation.services.strategies;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.models.ContextoZoomMapa;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.services.AdaptadorH3;

import java.util.List;
import java.util.Map;

/**
 * Patrón Strategy (Comportamiento - GoF): Define la interfaz para cambiar
 * dinámicamente el algoritmo de resolución espacial de celdas H3 según el
 * nivel de zoom del mapa interactivo (HU-05).
 */
public interface EstrategiaResolucionEspacial {

    /**
     * Evalúa si esta estrategia es aplicable para el nivel de zoom dado.
     * @param nivelZoom Nivel de zoom de Leaflet / Mapbox (ej. 5 a 18)
     */
    boolean soportaZoom(int nivelZoom);

    /**
     * Retorna la resolución H3 correspondiente (ej. 7, 8 o 9).
     */
    int getResolucionH3();

    /**
     * Retorna un nombre legible de la estrategia.
     */
    String getNombreEstrategia();

    /**
     * Agrega los reportes de emoción a la resolución de esta estrategia,
     * aplicando el criterio de Protección en Comunidad (mínimo k reportes por celda).
     */
    List<CeldaMapaEmocional> agregarCeldas(List<ReporteEmocion> reportes,
                                          ContextoZoomMapa contexto,
                                          Map<String, Zona> mapaZonas,
                                          AdaptadorH3 adaptadorH3);
}
