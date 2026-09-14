package com.almara.modules.geo.models;

/**
 * Objeto de contexto que transporta los parámetros disponibles para que las estrategias
 * determinen si aplican y resuelvan la celda H3.
 */
public class ResolucionZonaContexto {

    private final CoordenadasGps coordenadasGps;
    private final String zonaManualId;
    private final int resolucionDeseada;

    public ResolucionZonaContexto(CoordenadasGps coordenadasGps, String zonaManualId, int resolucionDeseada) {
        this.coordenadasGps = coordenadasGps;
        this.zonaManualId = zonaManualId;
        this.resolucionDeseada = resolucionDeseada;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public CoordenadasGps getCoordenadasGps() {
        return coordenadasGps;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public int getResolucionDeseada() {
        return resolucionDeseada;
    }

    public boolean tieneCoordenadasGps() {
        return coordenadasGps != null &&
               coordenadasGps.getLatitud() != null &&
               coordenadasGps.getLongitud() != null;
    }

    public boolean tieneZonaManual() {
        return zonaManualId != null && !zonaManualId.trim().isEmpty();
    }

    public static class Constructor {
        private CoordenadasGps coordenadasGps;
        private String zonaManualId;
        private int resolucionDeseada = 9;

        public Constructor coordenadasGps(CoordenadasGps coordenadasGps) {
            this.coordenadasGps = coordenadasGps;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor resolucionDeseada(int resolucionDeseada) {
            this.resolucionDeseada = resolucionDeseada;
            return this;
        }

        public ResolucionZonaContexto build() {
            return new ResolucionZonaContexto(coordenadasGps, zonaManualId, resolucionDeseada);
        }
    }
}
