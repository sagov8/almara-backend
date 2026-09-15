package com.almara.modules.aggregation.models;

/**
 * Parámetros de contexto para la selección de estrategia espacial por nivel de zoom (HU-05).
 */
public class ContextoZoomMapa {

    private final int nivelZoom;
    private final int umbralMinimoK;
    private final Double minLat;
    private final Double maxLat;
    private final Double minLon;
    private final Double maxLon;

    public ContextoZoomMapa(int nivelZoom, int umbralMinimoK, Double minLat, Double maxLat, Double minLon, Double maxLon) {
        this.nivelZoom = nivelZoom;
        this.umbralMinimoK = umbralMinimoK > 0 ? umbralMinimoK : 5;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public int getNivelZoom() {
        return nivelZoom;
    }

    public int getUmbralMinimoK() {
        return umbralMinimoK;
    }

    public Double getMinLat() {
        return minLat;
    }

    public Double getMaxLat() {
        return maxLat;
    }

    public Double getMinLon() {
        return minLon;
    }

    public Double getMaxLon() {
        return maxLon;
    }

    public boolean tieneFiltroGeografico() {
        return minLat != null && maxLat != null && minLon != null && maxLon != null;
    }

    public static class Constructor {
        private int nivelZoom = 14;
        private int umbralMinimoK = 5;
        private Double minLat;
        private Double maxLat;
        private Double minLon;
        private Double maxLon;

        public Constructor nivelZoom(int nivelZoom) {
            this.nivelZoom = nivelZoom;
            return this;
        }

        public Constructor umbralMinimoK(int umbralMinimoK) {
            this.umbralMinimoK = umbralMinimoK;
            return this;
        }

        public Constructor minLat(Double minLat) {
            this.minLat = minLat;
            return this;
        }

        public Constructor maxLat(Double maxLat) {
            this.maxLat = maxLat;
            return this;
        }

        public Constructor minLon(Double minLon) {
            this.minLon = minLon;
            return this;
        }

        public Constructor maxLon(Double maxLon) {
            this.maxLon = maxLon;
            return this;
        }

        public ContextoZoomMapa build() {
            return new ContextoZoomMapa(nivelZoom, umbralMinimoK, minLat, maxLat, minLon, maxLon);
        }
    }
}
