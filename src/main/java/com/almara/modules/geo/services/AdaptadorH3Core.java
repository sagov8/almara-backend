package com.almara.modules.geo.services;

import com.almara.modules.geo.models.CoordenadasGps;
import com.uber.h3core.H3Core;
import com.uber.h3core.util.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import java.util.List;

/**
 * Implementación concreta del patrón Adapter que envuelve la librería nativa Uber H3 Core.
 * Aísla al resto del sistema de cualquier detalle técnico específico de la implementación C/JNI de H3.
 */
@Component
public class AdaptadorH3Core implements AdaptadorH3 {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorH3Core.class);

    private final H3Core h3Core;

    public AdaptadorH3Core() {
        try {
            this.h3Core = H3Core.newInstance();
            log.info("Motor geoespacial Uber H3 Core inicializado exitosamente.");
        } catch (IOException ex) {
            log.error("Error al inicializar las librerías nativas de Uber H3 Core: {}", ex.getMessage(), ex);
            throw new IllegalStateException("No fue posible inicializar el motor espacial H3", ex);
        }
    }

    public AdaptadorH3Core(H3Core h3Core) {
        this.h3Core = h3Core;
    }

    @Override
    public String coordenadasACelda(double latitud, double longitud, int resolucion) {
        return h3Core.latLngToCellAddress(latitud, longitud, resolucion);
    }

    @Override
    public CoordenadasGps celdaACentroide(String idCeldaH3) {
        LatLng latLng = h3Core.cellToLatLng(idCeldaH3);
        return new CoordenadasGps(latLng.lat, latLng.lng);
    }

    @Override
    public boolean esCeldaValida(String idCeldaH3) {
        if (idCeldaH3 == null || idCeldaH3.trim().isEmpty()) {
            return false;
        }
        try {
            return h3Core.isValidCell(idCeldaH3);
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public int obtenerResolucion(String idCeldaH3) {
        return h3Core.getResolution(idCeldaH3);
    }

    @Override
    public String obtenerCeldaPadre(String idCeldaH3, int resolucionPadre) {
        if (!esCeldaValida(idCeldaH3)) {
            return idCeldaH3;
        }
        int resActual = obtenerResolucion(idCeldaH3);
        if (resolucionPadre >= resActual) {
            return idCeldaH3;
        }
        return h3Core.cellToParentAddress(idCeldaH3, resolucionPadre);
    }

    @Override
    public List<CoordenadasGps> obtenerLimitesHexagono(String idCeldaH3) {
        if (!esCeldaValida(idCeldaH3)) {
            return List.of();
        }
        List<LatLng> vertices = h3Core.cellToBoundary(idCeldaH3);
        return vertices.stream()
                .map(v -> new CoordenadasGps(v.lat, v.lng))
                .toList();
    }
}
