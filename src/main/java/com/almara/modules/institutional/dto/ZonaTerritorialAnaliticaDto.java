package com.almara.modules.institutional.dto;

import java.util.Map;

/**
 * DTO que representa los datos analíticos agregados de una zona territorial urbana de Popayán (HU-24).
 * Cumple estrictamente con el principio de k-anonimato (k >= 5) y Privacy by Design.
 */
public class ZonaTerritorialAnaliticaDto {

    private String idCeldaH3;
    private String zonaManualId;
    private String nombreZona;
    private String descripcionZona;
    private double latitudCentroide;
    private double longitudCentroide;
    private boolean cumpleUmbral;
    private String emocionPredominante;
    private String nombreEmocionPredominante;
    private String codigoHexColor;
    private String icono;
    private double intensidadPromedio;
    private String participantesAproximados;
    private Map<String, Integer> distribucionPorcentual;
    private String tendencia;

    public ZonaTerritorialAnaliticaDto() {
    }

    public ZonaTerritorialAnaliticaDto(String idCeldaH3, String zonaManualId, String nombreZona,
                                       String descripcionZona, double latitudCentroide, double longitudCentroide,
                                       boolean cumpleUmbral, String emocionPredominante,
                                       String nombreEmocionPredominante, String codigoHexColor,
                                       String icono, double intensidadPromedio, String participantesAproximados,
                                       Map<String, Integer> distribucionPorcentual, String tendencia) {
        this.idCeldaH3 = idCeldaH3;
        this.zonaManualId = zonaManualId;
        this.nombreZona = nombreZona;
        this.descripcionZona = descripcionZona;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
        this.cumpleUmbral = cumpleUmbral;
        this.emocionPredominante = emocionPredominante;
        this.nombreEmocionPredominante = nombreEmocionPredominante;
        this.codigoHexColor = codigoHexColor;
        this.icono = icono;
        this.intensidadPromedio = intensidadPromedio;
        this.participantesAproximados = participantesAproximados;
        this.distribucionPorcentual = distribucionPorcentual;
        this.tendencia = tendencia;
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

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public String getDescripcionZona() {
        return descripcionZona;
    }

    public void setDescripcionZona(String descripcionZona) {
        this.descripcionZona = descripcionZona;
    }

    public double getLatitudCentroide() {
        return latitudCentroide;
    }

    public void setLatitudCentroide(double latitudCentroide) {
        this.latitudCentroide = latitudCentroide;
    }

    public double getLongitudCentroide() {
        return longitudCentroide;
    }

    public void setLongitudCentroide(double longitudCentroide) {
        this.longitudCentroide = longitudCentroide;
    }

    public boolean isCumpleUmbral() {
        return cumpleUmbral;
    }

    public void setCumpleUmbral(boolean cumpleUmbral) {
        this.cumpleUmbral = cumpleUmbral;
    }

    public String getEmocionPredominante() {
        return emocionPredominante;
    }

    public void setEmocionPredominante(String emocionPredominante) {
        this.emocionPredominante = emocionPredominante;
    }

    public String getNombreEmocionPredominante() {
        return nombreEmocionPredominante;
    }

    public void setNombreEmocionPredominante(String nombreEmocionPredominante) {
        this.nombreEmocionPredominante = nombreEmocionPredominante;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = codigoHexColor;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public double getIntensidadPromedio() {
        return intensidadPromedio;
    }

    public void setIntensidadPromedio(double intensidadPromedio) {
        this.intensidadPromedio = intensidadPromedio;
    }

    public String getParticipantesAproximados() {
        return participantesAproximados;
    }

    public void setParticipantesAproximados(String participantesAproximados) {
        this.participantesAproximados = participantesAproximados;
    }

    public Map<String, Integer> getDistribucionPorcentual() {
        return distribucionPorcentual;
    }

    public void setDistribucionPorcentual(Map<String, Integer> distribucionPorcentual) {
        this.distribucionPorcentual = distribucionPorcentual;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public static class Constructor {
        private String idCeldaH3;
        private String zonaManualId;
        private String nombreZona;
        private String descripcionZona;
        private double latitudCentroide;
        private double longitudCentroide;
        private boolean cumpleUmbral;
        private String emocionPredominante;
        private String nombreEmocionPredominante;
        private String codigoHexColor;
        private String icono;
        private double intensidadPromedio;
        private String participantesAproximados;
        private Map<String, Integer> distribucionPorcentual;
        private String tendencia;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor nombreZona(String nombreZona) {
            this.nombreZona = nombreZona;
            return this;
        }

        public Constructor descripcionZona(String descripcionZona) {
            this.descripcionZona = descripcionZona;
            return this;
        }

        public Constructor latitudCentroide(double latitudCentroide) {
            this.latitudCentroide = latitudCentroide;
            return this;
        }

        public Constructor longitudCentroide(double longitudCentroide) {
            this.longitudCentroide = longitudCentroide;
            return this;
        }

        public Constructor cumpleUmbral(boolean cumpleUmbral) {
            this.cumpleUmbral = cumpleUmbral;
            return this;
        }

        public Constructor emocionPredominante(String emocionPredominante) {
            this.emocionPredominante = emocionPredominante;
            return this;
        }

        public Constructor nombreEmocionPredominante(String nombreEmocionPredominante) {
            this.nombreEmocionPredominante = nombreEmocionPredominante;
            return this;
        }

        public Constructor codigoHexColor(String codigoHexColor) {
            this.codigoHexColor = codigoHexColor;
            return this;
        }

        public Constructor icono(String icono) {
            this.icono = icono;
            return this;
        }

        public Constructor intensidadPromedio(double intensidadPromedio) {
            this.intensidadPromedio = intensidadPromedio;
            return this;
        }

        public Constructor participantesAproximados(String participantesAproximados) {
            this.participantesAproximados = participantesAproximados;
            return this;
        }

        public Constructor distribucionPorcentual(Map<String, Integer> distribucionPorcentual) {
            this.distribucionPorcentual = distribucionPorcentual;
            return this;
        }

        public Constructor tendencia(String tendencia) {
            this.tendencia = tendencia;
            return this;
        }

        public ZonaTerritorialAnaliticaDto build() {
            return new ZonaTerritorialAnaliticaDto(
                    idCeldaH3, zonaManualId, nombreZona, descripcionZona, latitudCentroide, longitudCentroide,
                    cumpleUmbral, emocionPredominante, nombreEmocionPredominante, codigoHexColor, icono,
                    intensidadPromedio, participantesAproximados, distribucionPorcentual, tendencia
            );
        }
    }
}
