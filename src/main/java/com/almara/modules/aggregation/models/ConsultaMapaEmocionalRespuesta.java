package com.almara.modules.aggregation.models;

import java.util.List;

/**
 * Respuesta que encapsula la capa emocional visible para el mapa interactivo (HU-05).
 * Comunica al usuario común la protección en comunidad de forma amigable y sin jerga técnica.
 */
public class ConsultaMapaEmocionalRespuesta {

    private final List<CeldaMapaEmocional> celdasVisibles;
    private final int totalCeldasVisibles;
    private final int totalReportesEnMapa;
    private final int umbralMinimoAplicado;
    private final int resolucionH3Utilizada;
    private final String mensajeProteccionComunidad;
    private final long tiempoCalculoMs;

    public ConsultaMapaEmocionalRespuesta(List<CeldaMapaEmocional> celdasVisibles, int totalCeldasVisibles,
                                         int totalReportesEnMapa, int umbralMinimoAplicado,
                                         int resolucionH3Utilizada, String mensajeProteccionComunidad,
                                         long tiempoCalculoMs) {
        this.celdasVisibles = celdasVisibles;
        this.totalCeldasVisibles = totalCeldasVisibles;
        this.totalReportesEnMapa = totalReportesEnMapa;
        this.umbralMinimoAplicado = umbralMinimoAplicado;
        this.resolucionH3Utilizada = resolucionH3Utilizada;
        this.mensajeProteccionComunidad = mensajeProteccionComunidad;
        this.tiempoCalculoMs = tiempoCalculoMs;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public List<CeldaMapaEmocional> getCeldasVisibles() {
        return celdasVisibles;
    }

    public int getTotalCeldasVisibles() {
        return totalCeldasVisibles;
    }

    public int getTotalReportesEnMapa() {
        return totalReportesEnMapa;
    }

    public int getUmbralMinimoAplicado() {
        return umbralMinimoAplicado;
    }

    public int getResolucionH3Utilizada() {
        return resolucionH3Utilizada;
    }

    public String getMensajeProteccionComunidad() {
        return mensajeProteccionComunidad;
    }

    public long getTiempoCalculoMs() {
        return tiempoCalculoMs;
    }

    public static class Constructor {
        private List<CeldaMapaEmocional> celdasVisibles;
        private int totalCeldasVisibles;
        private int totalReportesEnMapa;
        private int umbralMinimoAplicado;
        private int resolucionH3Utilizada;
        private String mensajeProteccionComunidad;
        private long tiempoCalculoMs;

        public Constructor celdasVisibles(List<CeldaMapaEmocional> celdasVisibles) {
            this.celdasVisibles = celdasVisibles;
            return this;
        }

        public Constructor totalCeldasVisibles(int totalCeldasVisibles) {
            this.totalCeldasVisibles = totalCeldasVisibles;
            return this;
        }

        public Constructor totalReportesEnMapa(int totalReportesEnMapa) {
            this.totalReportesEnMapa = totalReportesEnMapa;
            return this;
        }

        public Constructor umbralMinimoAplicado(int umbralMinimoAplicado) {
            this.umbralMinimoAplicado = umbralMinimoAplicado;
            return this;
        }

        public Constructor resolucionH3Utilizada(int resolucionH3Utilizada) {
            this.resolucionH3Utilizada = resolucionH3Utilizada;
            return this;
        }

        public Constructor mensajeProteccionComunidad(String mensajeProteccionComunidad) {
            this.mensajeProteccionComunidad = mensajeProteccionComunidad;
            return this;
        }

        public Constructor tiempoCalculoMs(long tiempoCalculoMs) {
            this.tiempoCalculoMs = tiempoCalculoMs;
            return this;
        }

        public ConsultaMapaEmocionalRespuesta build() {
            return new ConsultaMapaEmocionalRespuesta(celdasVisibles, totalCeldasVisibles, totalReportesEnMapa,
                    umbralMinimoAplicado, resolucionH3Utilizada, mensajeProteccionComunidad, tiempoCalculoMs);
        }
    }
}
