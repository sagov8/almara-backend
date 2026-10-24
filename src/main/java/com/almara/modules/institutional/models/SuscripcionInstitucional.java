package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de Suscripción para clientes institucionales.
 */
@Entity
@Table(name = "suscripcion_institucional")
public class SuscripcionInstitucional {

    @Id
    @Column(name = "id_suscripcion")
    private UUID idSuscripcion;

    @Column(name = "id_institucion", nullable = false)
    private UUID idInstitucion;

    @Column(name = "id_plan", nullable = false)
    private UUID idPlan;

    @Column(name = "estado_suscripcion", length = 25, nullable = false)
    private String estadoSuscripcion;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion = Instant.now();

    public SuscripcionInstitucional() {
    }

    public SuscripcionInstitucional(UUID idSuscripcion, UUID idInstitucion, UUID idPlan, String estadoSuscripcion,
                                    Instant fechaInicio, Instant fechaExpiracion, Instant fechaActualizacion) {
        this.idSuscripcion = idSuscripcion;
        this.idInstitucion = idInstitucion;
        this.idPlan = idPlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.fechaActualizacion = fechaActualizacion != null ? fechaActualizacion : Instant.now();
    }

    public boolean estaVigente() {
        return "ACTIVA".equalsIgnoreCase(this.estadoSuscripcion)
                && this.fechaExpiracion != null
                && this.fechaExpiracion.isAfter(Instant.now());
    }

    public void revocarPorExpiracion() {
        this.estadoSuscripcion = "EXPIRADA";
        this.fechaActualizacion = Instant.now();
    }

    public void renovarSuscripcion(int dias) {
        if (dias <= 0) {
            dias = 30;
        }
        Instant base = estaVigente() ? this.fechaExpiracion : Instant.now();
        this.fechaExpiracion = base.plusSeconds((long) dias * 86400L);
        this.estadoSuscripcion = "ACTIVA";
        this.fechaActualizacion = Instant.now();
    }

    public long calcularDiasRestantes() {
        if (!estaVigente() || this.fechaExpiracion == null) {
            return 0L;
        }
        long segundos = java.time.Duration.between(Instant.now(), this.fechaExpiracion).getSeconds();
        if (segundos <= 0) {
            return 0L;
        }
        return (segundos + 86399L) / 86400L;
    }

    public UUID getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(UUID idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public UUID getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(UUID idPlan) {
        this.idPlan = idPlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Instant fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Instant fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idSuscripcion;
        private UUID idInstitucion;
        private UUID idPlan;
        private String estadoSuscripcion = "ACTIVA";
        private Instant fechaInicio = Instant.now();
        private Instant fechaExpiracion = Instant.now().plusSeconds(30L * 86400L);
        private Instant fechaActualizacion = Instant.now();

        public Constructor idSuscripcion(UUID idSuscripcion) {
            this.idSuscripcion = idSuscripcion;
            return this;
        }

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor idPlan(UUID idPlan) {
            this.idPlan = idPlan;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor fechaInicio(Instant fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public Constructor fechaExpiracion(Instant fechaExpiracion) {
            this.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public Constructor fechaActualizacion(Instant fechaActualizacion) {
            this.fechaActualizacion = fechaActualizacion;
            return this;
        }

        public SuscripcionInstitucional build() {
            return new SuscripcionInstitucional(idSuscripcion, idInstitucion, idPlan, estadoSuscripcion,
                    fechaInicio, fechaExpiracion, fechaActualizacion);
        }
    }
}
