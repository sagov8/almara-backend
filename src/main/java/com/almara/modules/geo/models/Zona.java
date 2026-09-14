package com.almara.modules.geo.models;

/**
 * Modelo de dominio que representa una zona geográfica / celda H3 urbana.
 * Basado en el esquema de base de datos de Almara (Tabla Zona).
 */
public class Zona {

    private String idCeldaH3;
    private int resolucionH3;
    private Double latitudCentroide;
    private Double longitudCentroide;
    private String zonaManualId;
    private String nombreZonaManual;
    private int umbralMinimo;

    public Zona() {
    }

    public Zona(String idCeldaH3, int resolucionH3, Double latitudCentroide, Double longitudCentroide,
                String zonaManualId, String nombreZonaManual, int umbralMinimo) {
        this.idCeldaH3 = idCeldaH3;
        this.resolucionH3 = resolucionH3;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
        this.zonaManualId = zonaManualId;
        this.nombreZonaManual = nombreZonaManual;
        this.umbralMinimo = umbralMinimo;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public int getResolucionH3() {
        return resolucionH3;
    }

    public void setResolucionH3(int resolucionH3) {
        this.resolucionH3 = resolucionH3;
    }

    public Double getLatitudCentroide() {
        return latitudCentroide;
    }

    public void setLatitudCentroide(Double latitudCentroide) {
        this.latitudCentroide = latitudCentroide;
    }

    public Double getLongitudCentroide() {
        return longitudCentroide;
    }

    public void setLongitudCentroide(Double longitudCentroide) {
        this.longitudCentroide = longitudCentroide;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public String getNombreZonaManual() {
        return nombreZonaManual;
    }

    public void setNombreZonaManual(String nombreZonaManual) {
        this.nombreZonaManual = nombreZonaManual;
    }

    public int getUmbralMinimo() {
        return umbralMinimo;
    }

    public void setUmbralMinimo(int umbralMinimo) {
        this.umbralMinimo = umbralMinimo;
    }

    public static class Constructor {
        private String idCeldaH3;
        private int resolucionH3;
        private Double latitudCentroide;
        private Double longitudCentroide;
        private String zonaManualId;
        private String nombreZonaManual;
        private int umbralMinimo = 5;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor resolucionH3(int resolucionH3) {
            this.resolucionH3 = resolucionH3;
            return this;
        }

        public Constructor latitudCentroide(Double latitudCentroide) {
            this.latitudCentroide = latitudCentroide;
            return this;
        }

        public Constructor longitudCentroide(Double longitudCentroide) {
            this.longitudCentroide = longitudCentroide;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor nombreZonaManual(String nombreZonaManual) {
            this.nombreZonaManual = nombreZonaManual;
            return this;
        }

        public Constructor umbralMinimo(int umbralMinimo) {
            this.umbralMinimo = umbralMinimo;
            return this;
        }

        public Zona build() {
            return new Zona(idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide,
                    zonaManualId, nombreZonaManual, umbralMinimo);
        }
    }
}
