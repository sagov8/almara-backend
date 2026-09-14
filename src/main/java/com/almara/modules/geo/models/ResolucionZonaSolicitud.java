package com.almara.modules.geo.models;

import jakarta.validation.Valid;

/**
 * DTO para la petición de resolución de celda geográfica desde el controlador geo.
 * Admite coordenadas GPS temporales o el ID de una zona del catálogo manual.
 */
public class ResolucionZonaSolicitud {

    @Valid
    private CoordenadasGps coordenadasGps;

    private String zonaManualId;

    public ResolucionZonaSolicitud() {
    }

    public ResolucionZonaSolicitud(CoordenadasGps coordenadasGps, String zonaManualId) {
        this.coordenadasGps = coordenadasGps;
        this.zonaManualId = zonaManualId;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public CoordenadasGps getCoordenadasGps() {
        return coordenadasGps;
    }

    public void setCoordenadasGps(CoordenadasGps coordenadasGps) {
        this.coordenadasGps = coordenadasGps;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public static class Constructor {
        private CoordenadasGps coordenadasGps;
        private String zonaManualId;

        public Constructor coordenadasGps(CoordenadasGps coordenadasGps) {
            this.coordenadasGps = coordenadasGps;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public ResolucionZonaSolicitud build() {
            return new ResolucionZonaSolicitud(coordenadasGps, zonaManualId);
        }
    }
}
