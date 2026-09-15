package com.almara.modules.aggregation.models;

import com.almara.modules.emotion.models.TipoEmocion;

import java.util.List;
import java.util.Map;

/**
 * DTO que encapsula el detalle multidimensional de una celda seleccionada (HU-06).
 * Cumple con RNF Privacidad (participantes aproximados) y RNF Completitud (porcentajes = 100%).
 */
public class DetalleZonaEmocionRespuesta {

    private String idCeldaH3;
    private String nombreZona;
    private TipoEmocion emocionPredominante;
    private String nombreEmocion;
    private String codigoHexColor;
    private String codigoHexFondo;
    private String icono;
    private Map<String, Integer> distribucionPorcentajes;
    private String tendencia;
    private String participantesAproximados;
    private String periodoTemporal;
    private List<String> comentariosRecientes;
    private boolean cumpleUmbral;
    private String descripcionProteccion;
    private long tiempoCalculoMs;

    public DetalleZonaEmocionRespuesta() {
    }

    public DetalleZonaEmocionRespuesta(String idCeldaH3, String nombreZona, TipoEmocion emocionPredominante,
                                       String nombreEmocion, String codigoHexColor, String codigoHexFondo,
                                       String icono, Map<String, Integer> distribucionPorcentajes,
                                       String tendencia, String participantesAproximados,
                                       String periodoTemporal, List<String> comentariosRecientes,
                                       boolean cumpleUmbral, String descripcionProteccion, long tiempoCalculoMs) {
        this.idCeldaH3 = idCeldaH3;
        this.nombreZona = nombreZona;
        this.emocionPredominante = emocionPredominante;
        this.nombreEmocion = nombreEmocion;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.icono = icono;
        this.distribucionPorcentajes = distribucionPorcentajes;
        this.tendencia = tendencia;
        this.participantesAproximados = participantesAproximados;
        this.periodoTemporal = periodoTemporal;
        this.comentariosRecientes = comentariosRecientes;
        this.cumpleUmbral = cumpleUmbral;
        this.descripcionProteccion = descripcionProteccion;
        this.tiempoCalculoMs = tiempoCalculoMs;
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

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public TipoEmocion getEmocionPredominante() {
        return emocionPredominante;
    }

    public void setEmocionPredominante(TipoEmocion emocionPredominante) {
        this.emocionPredominante = emocionPredominante;
    }

    public String getNombreEmocion() {
        return nombreEmocion;
    }

    public void setNombreEmocion(String nombreEmocion) {
        this.nombreEmocion = nombreEmocion;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = codigoHexColor;
    }

    public String getCodigoHexFondo() {
        return codigoHexFondo;
    }

    public void setCodigoHexFondo(String codigoHexFondo) {
        this.codigoHexFondo = codigoHexFondo;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public Map<String, Integer> getDistribucionPorcentajes() {
        return distribucionPorcentajes;
    }

    public void setDistribucionPorcentajes(Map<String, Integer> distribucionPorcentajes) {
        this.distribucionPorcentajes = distribucionPorcentajes;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public String getParticipantesAproximados() {
        return participantesAproximados;
    }

    public void setParticipantesAproximados(String participantesAproximados) {
        this.participantesAproximados = participantesAproximados;
    }

    public String getPeriodoTemporal() {
        return periodoTemporal;
    }

    public void setPeriodoTemporal(String periodoTemporal) {
        this.periodoTemporal = periodoTemporal;
    }

    public List<String> getComentariosRecientes() {
        return comentariosRecientes;
    }

    public void setComentariosRecientes(List<String> comentariosRecientes) {
        this.comentariosRecientes = comentariosRecientes;
    }

    public boolean isCumpleUmbral() {
        return cumpleUmbral;
    }

    public void setCumpleUmbral(boolean cumpleUmbral) {
        this.cumpleUmbral = cumpleUmbral;
    }

    public String getDescripcionProteccion() {
        return descripcionProteccion;
    }

    public void setDescripcionProteccion(String descripcionProteccion) {
        this.descripcionProteccion = descripcionProteccion;
    }

    public long getTiempoCalculoMs() {
        return tiempoCalculoMs;
    }

    public void setTiempoCalculoMs(long tiempoCalculoMs) {
        this.tiempoCalculoMs = tiempoCalculoMs;
    }

    public static class Constructor {
        private String idCeldaH3;
        private String nombreZona;
        private TipoEmocion emocionPredominante;
        private String nombreEmocion;
        private String codigoHexColor;
        private String codigoHexFondo;
        private String icono;
        private Map<String, Integer> distribucionPorcentajes;
        private String tendencia;
        private String participantesAproximados;
        private String periodoTemporal;
        private List<String> comentariosRecientes;
        private boolean cumpleUmbral;
        private String descripcionProteccion;
        private long tiempoCalculoMs;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor nombreZona(String nombreZona) {
            this.nombreZona = nombreZona;
            return this;
        }

        public Constructor emocionPredominante(TipoEmocion emocionPredominante) {
            this.emocionPredominante = emocionPredominante;
            return this;
        }

        public Constructor nombreEmocion(String nombreEmocion) {
            this.nombreEmocion = nombreEmocion;
            return this;
        }

        public Constructor codigoHexColor(String codigoHexColor) {
            this.codigoHexColor = codigoHexColor;
            return this;
        }

        public Constructor codigoHexFondo(String codigoHexFondo) {
            this.codigoHexFondo = codigoHexFondo;
            return this;
        }

        public Constructor icono(String icono) {
            this.icono = icono;
            return this;
        }

        public Constructor distribucionPorcentajes(Map<String, Integer> distribucionPorcentajes) {
            this.distribucionPorcentajes = distribucionPorcentajes;
            return this;
        }

        public Constructor tendencia(String tendencia) {
            this.tendencia = tendencia;
            return this;
        }

        public Constructor participantesAproximados(String participantesAproximados) {
            this.participantesAproximados = participantesAproximados;
            return this;
        }

        public Constructor periodoTemporal(String periodoTemporal) {
            this.periodoTemporal = periodoTemporal;
            return this;
        }

        public Constructor comentariosRecientes(List<String> comentariosRecientes) {
            this.comentariosRecientes = comentariosRecientes;
            return this;
        }

        public Constructor cumpleUmbral(boolean cumpleUmbral) {
            this.cumpleUmbral = cumpleUmbral;
            return this;
        }

        public Constructor descripcionProteccion(String descripcionProteccion) {
            this.descripcionProteccion = descripcionProteccion;
            return this;
        }

        public Constructor tiempoCalculoMs(long tiempoCalculoMs) {
            this.tiempoCalculoMs = tiempoCalculoMs;
            return this;
        }

        public DetalleZonaEmocionRespuesta build() {
            return new DetalleZonaEmocionRespuesta(idCeldaH3, nombreZona, emocionPredominante,
                    nombreEmocion, codigoHexColor, codigoHexFondo, icono, distribucionPorcentajes,
                    tendencia, participantesAproximados, periodoTemporal, comentariosRecientes,
                    cumpleUmbral, descripcionProteccion, tiempoCalculoMs);
        }
    }
}
