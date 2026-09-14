package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio que representa un reporte de emoción ciudadana registrado en el sistema.
 * Basado en la tabla ReporteEmocion de la base de datos de Almara.
 * Garantiza Privacy by Design: almacena idCeldaH3 y jamás almacena coordenadas GPS.
 */
public class ReporteEmocion {

    private UUID idEvento;
    private String idCeldaH3;
    private TipoEmocion emocion;
    private Float intensidad;
    private String comentario;
    private String tokenSesionTemporal;
    private Instant fechaHora;

    public ReporteEmocion() {
    }

    public ReporteEmocion(UUID idEvento, String idCeldaH3, TipoEmocion emocion,
                          Float intensidad, String comentario, String tokenSesionTemporal,
                          Instant fechaHora) {
        this.idEvento = idEvento;
        this.idCeldaH3 = idCeldaH3;
        this.emocion = emocion;
        this.intensidad = intensidad;
        this.comentario = comentario;
        this.tokenSesionTemporal = tokenSesionTemporal;
        this.fechaHora = fechaHora;
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

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public TipoEmocion getEmocion() {
        return emocion;
    }

    public void setEmocion(TipoEmocion emocion) {
        this.emocion = emocion;
    }

    public Float getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(Float intensidad) {
        this.intensidad = intensidad;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getTokenSesionTemporal() {
        return tokenSesionTemporal;
    }

    public void setTokenSesionTemporal(String tokenSesionTemporal) {
        this.tokenSesionTemporal = tokenSesionTemporal;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public static class Constructor {
        private UUID idEvento;
        private String idCeldaH3;
        private TipoEmocion emocion;
        private Float intensidad;
        private String comentario;
        private String tokenSesionTemporal;
        private Instant fechaHora;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor intensidad(Float intensidad) {
            this.intensidad = intensidad;
            return this;
        }

        public Constructor comentario(String comentario) {
            this.comentario = comentario;
            return this;
        }

        public Constructor tokenSesionTemporal(String tokenSesionTemporal) {
            this.tokenSesionTemporal = tokenSesionTemporal;
            return this;
        }

        public Constructor fechaHora(Instant fechaHora) {
            this.fechaHora = fechaHora;
            return this;
        }

        public ReporteEmocion build() {
            return new ReporteEmocion(idEvento, idCeldaH3, emocion, intensidad, comentario, tokenSesionTemporal, fechaHora);
        }
    }
}
