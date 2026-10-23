package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de auditoría institucional e histórico de suscripciones (RNF-13).
 */
@Entity
@Table(name = "auditoria_institucional")
public class AuditoriaInstitucional {

    @Id
    @Column(name = "id_auditoria")
    private UUID idAuditoria;

    @Column(name = "id_entidad_afectada", nullable = false)
    private UUID idEntidadAfectada;

    @Column(name = "tipo_evento", length = 50, nullable = false)
    private String tipoEvento;

    @Column(name = "detalle", length = 255, nullable = false)
    private String detalle;

    @Column(name = "actor_responsable", length = 100, nullable = false)
    private String actorResponsable;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento = Instant.now();

    public AuditoriaInstitucional() {
    }

    public AuditoriaInstitucional(UUID idAuditoria, UUID idEntidadAfectada, String tipoEvento, String detalle,
                                  String actorResponsable, Instant fechaEvento) {
        this.idAuditoria = idAuditoria;
        this.idEntidadAfectada = idEntidadAfectada;
        this.tipoEvento = tipoEvento;
        this.detalle = detalle;
        this.actorResponsable = actorResponsable;
        this.fechaEvento = fechaEvento != null ? fechaEvento : Instant.now();
    }

    public UUID getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(UUID idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public UUID getIdEntidadAfectada() {
        return idEntidadAfectada;
    }

    public void setIdEntidadAfectada(UUID idEntidadAfectada) {
        this.idEntidadAfectada = idEntidadAfectada;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getActorResponsable() {
        return actorResponsable;
    }

    public void setActorResponsable(String actorResponsable) {
        this.actorResponsable = actorResponsable;
    }

    public Instant getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Instant fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idAuditoria;
        private UUID idEntidadAfectada;
        private String tipoEvento;
        private String detalle;
        private String actorResponsable;
        private Instant fechaEvento = Instant.now();

        public Constructor idAuditoria(UUID idAuditoria) {
            this.idAuditoria = idAuditoria;
            return this;
        }

        public Constructor idEntidadAfectada(UUID idEntidadAfectada) {
            this.idEntidadAfectada = idEntidadAfectada;
            return this;
        }

        public Constructor tipoEvento(String tipoEvento) {
            this.tipoEvento = tipoEvento;
            return this;
        }

        public Constructor detalle(String detalle) {
            this.detalle = detalle;
            return this;
        }

        public Constructor actorResponsable(String actorResponsable) {
            this.actorResponsable = actorResponsable;
            return this;
        }

        public Constructor fechaEvento(Instant fechaEvento) {
            this.fechaEvento = fechaEvento;
            return this;
        }

        public AuditoriaInstitucional build() {
            return new AuditoriaInstitucional(idAuditoria, idEntidadAfectada, tipoEvento, detalle,
                    actorResponsable, fechaEvento);
        }
    }
}
