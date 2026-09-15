package com.almara.modules.aggregation.services.strategies;

import org.springframework.stereotype.Component;

/**
 * Estrategia de resolución H3 para nivel de zoom intermedio (11 <= Zoom <= 13).
 * Emplea resolución 8 (~450m de radio, nivel de comuna / sector urbano).
 */
@Component
public class EstrategiaZoomMedio extends EstrategiaResolucionBase {

    public static final int RESOLUCION_H3_MEDIO = 8;
    public static final int ZOOM_MINIMO_MEDIO = 11;
    public static final int ZOOM_MAXIMO_MEDIO = 13;

    @Override
    public boolean soportaZoom(int nivelZoom) {
        return nivelZoom >= ZOOM_MINIMO_MEDIO && nivelZoom <= ZOOM_MAXIMO_MEDIO;
    }

    @Override
    public int getResolucionH3() {
        return RESOLUCION_H3_MEDIO;
    }

    @Override
    public String getNombreEstrategia() {
        return "Sector Intermedio (Resolución H3: 8)";
    }
}
