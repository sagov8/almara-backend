package com.almara.modules.geo.models;

/**
 * Resultado de la resolución espacial hacia celda H3.
 * Contiene el id de celda H3, resolución y metadatos de zona.
 * No contiene coordenadas precisas del usuario.
 */
public class ResultadoResolucionZona {

    private String idCeldaH3;
    private int resolucionH3;
    private String nombreZona;
    private String zonaManualId;
    private boolean esManual;
    private Double latitudCentroide;
    private Double longitudCentroide;

    public ResultadoResolucionZona() {
    }

    public ResultadoResolucionZona(String idCeldaH3, int resolucionH3, String nombreZona,
                                   String zonaManualId, boolean esManual,
                                   Double latitudCentroide, Double longitudCentroide) {
        this.idCeldaH3 = idCeldaH3;
        this.resolucionH3 = resolucionH3;
        this.nombreZona = nombreZona;
        this.zonaManualId = zonaManualId;
        this.esManual = esManual;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
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

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public boolean isEsManual() {
        return esManual;
    }

    public void setEsManual(boolean esManual) {
        this.esManual = esManual;
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

    public static class Constructor {
        private String idCeldaH3;
        private int resolucionH3;
        private String nombreZona;
        private String zonaManualId;
        private boolean esManual;
        private Double latitudCentroide;
        private Double longitudCentroide;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor resolucionH3(int resolucionH3) {
            this.resolucionH3 = resolucionH3;
            return this;
        }

        public Constructor nombreZona(String nombreZona) {
            this.nombreZona = nombreZona;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor esManual(boolean esManual) {
            this.esManual = esManual;
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

        public ResultadoResolucionZona build() {
            return new ResultadoResolucionZona(idCeldaH3, resolucionH3, nombreZona, zonaManualId, esManual, latitudCentroide, longitudCentroide);
        }
    }
}
