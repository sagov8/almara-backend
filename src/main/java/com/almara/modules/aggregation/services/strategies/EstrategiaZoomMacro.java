package com.almara.modules.aggregation.services.strategies;

import org.springframework.stereotype.Component;

/**
 * Estrategia de resolución H3 para nivel de zoom macro o panorámico (Zoom < 11).
 * Emplea resolución 7 (~1.2km de radio, nivel de ciudad / metropolitano).
 */
@Component
public class EstrategiaZoomMacro extends EstrategiaResolucionBase {

    public static final int RESOLUCION_H3_MACRO = 7;
    public static final int ZOOM_LIMITE_MACRO = 11;

    @Override
    public boolean soportaZoom(int nivelZoom) {
        return nivelZoom < ZOOM_LIMITE_MACRO;
    }

    @Override
    public int getResolucionH3() {
        return RESOLUCION_H3_MACRO;
    }

    @Override
    public String getNombreEstrategia() {
        return "Panorámico Metropolitano (Resolución H3: 7)";
    }
}
