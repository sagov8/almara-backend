package com.almara.modules.aggregation.services.strategies;

import org.springframework.stereotype.Component;

/**
 * Estrategia de resolución H3 para nivel de zoom de detalle (Zoom >= 14).
 * Emplea resolución 9 (~100m de radio, nivel de calle / manzana barrial).
 */
@Component
public class EstrategiaZoomDetalle extends EstrategiaResolucionBase {

    public static final int RESOLUCION_H3_DETALLE = 9;
    public static final int ZOOM_MINIMO_DETALLE = 14;

    @Override
    public boolean soportaZoom(int nivelZoom) {
        return nivelZoom >= ZOOM_MINIMO_DETALLE;
    }

    @Override
    public int getResolucionH3() {
        return RESOLUCION_H3_DETALLE;
    }

    @Override
    public String getNombreEstrategia() {
        return "Detalle Barrial (Resolución H3: 9)";
    }
}
