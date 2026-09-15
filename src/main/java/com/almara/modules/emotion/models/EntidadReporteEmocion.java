package com.almara.modules.emotion.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA mapeada a la tabla reporte_emocion (almara_dbdiagram.dbml).
 * Garantiza Privacy by Design: almacena id_celda_h3 e id_emocion; nunca almacena coordenadas GPS.
 */
@Entity
@Table(name = "reporte_emocion")
public class EntidadReporteEmocion {

    @Id
    @Column(name = "id_evento", nullable = false)
    private UUID idEvento;

    @Column(name = "id_celda_h3", length = 15, nullable = false)
    private String idCeldaH3;

    @Column(name = "id_emocion", length = 20, nullable = false)
    private String idEmocion;

    @Column(name = "intensidad")
    private Float intensidad;

    @Column(name = "comentario", length = 200)
    private String comentario;

    @Column(name = "token_sesion_temporal", length = 64, nullable = false)
    private String tokenSesionTemporal;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    public EntidadReporteEmocion() {
    }

    public EntidadReporteEmocion(UUID idEvento, String idCeldaH3, String idEmocion,
                                 Float intensidad, String comentario,
                                 String tokenSesionTemporal, Instant fechaHora) {
        this.idEvento = idEvento;
        this.idCeldaH3 = idCeldaH3;
        this.idEmocion = idEmocion;
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

    public String getIdEmocion() {
        return idEmocion;
    }

    public void setIdEmocion(String idEmocion) {
        this.idEmocion = idEmocion;
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
        private String idEmocion;
        private Float intensidad;
        private String comentario;
        private String tokenSesionTemporal;
        private Instant fechaHora = Instant.now();

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor idEmocion(String idEmocion) {
            this.idEmocion = idEmocion;
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

        public EntidadReporteEmocion build() {
            return new EntidadReporteEmocion(idEvento, idCeldaH3, idEmocion, intensidad, comentario, tokenSesionTemporal, fechaHora);
        }
    }
}
