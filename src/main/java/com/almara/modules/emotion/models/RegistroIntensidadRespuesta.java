package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) de respuesta para la confirmación de registro de intensidad (HU-02).
 * Proporciona confirmación inmediata (< 300 ms) sin exponer datos de identidad (Privacy by Design).
 */
public class RegistroIntensidadRespuesta {

    private UUID idEvento;
    private Integer nivelIntensidad;
    private String etiquetaIntensidad;
    private TipoEmocion emocion;
    private String idCeldaH3;
    private String mensaje;
    private Instant fechaHoraEnvio;

    public RegistroIntensidadRespuesta() {
    }

    public RegistroIntensidadRespuesta(UUID idEvento, Integer nivelIntensidad, String etiquetaIntensidad,
                                      TipoEmocion emocion, String idCeldaH3, String mensaje,
                                      Instant fechaHoraEnvio) {
        this.idEvento = idEvento;
        this.nivelIntensidad = nivelIntensidad;
        this.etiquetaIntensidad = etiquetaIntensidad;
        this.emocion = emocion;
        this.idCeldaH3 = idCeldaH3;
        this.mensaje = mensaje;
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(UUID idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getNivelIntensidad() {
        return nivelIntensidad;
    }

    public void setNivelIntensidad(Integer nivelIntensidad) {
        this.nivelIntensidad = nivelIntensidad;
    }

    public String getEtiquetaIntensidad() {
        return etiquetaIntensidad;
    }

    public void setEtiquetaIntensidad(String etiquetaIntensidad) {
        this.etiquetaIntensidad = etiquetaIntensidad;
    }

    public TipoEmocion getEmocion() {
        return emocion;
    }

    public void setEmocion(TipoEmocion emocion) {
        this.emocion = emocion;
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    public void setFechaHoraEnvio(Instant fechaHoraEnvio) {
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    public static class Constructor {
        private UUID idEvento;
        private Integer nivelIntensidad;
        private String etiquetaIntensidad;
        private TipoEmocion emocion;
        private String idCeldaH3;
        private String mensaje;
        private Instant fechaHoraEnvio;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor nivelIntensidad(Integer nivelIntensidad) {
            this.nivelIntensidad = nivelIntensidad;
            return this;
        }

        public Constructor etiquetaIntensidad(String etiquetaIntensidad) {
            this.etiquetaIntensidad = etiquetaIntensidad;
            return this;
        }

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Constructor fechaHoraEnvio(Instant fechaHoraEnvio) {
            this.fechaHoraEnvio = fechaHoraEnvio;
            return this;
        }

        public RegistroIntensidadRespuesta build() {
            return new RegistroIntensidadRespuesta(idEvento, nivelIntensidad, etiquetaIntensidad, emocion, idCeldaH3, mensaje, fechaHoraEnvio);
        }
    }
}
