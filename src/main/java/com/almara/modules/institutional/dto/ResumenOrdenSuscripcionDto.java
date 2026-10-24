package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO que resume una orden de pedido de suscripción para el historial institucional (HU-21).
 */
public class ResumenOrdenSuscripcionDto {

    private UUID idOrden;
    private String numeroOrden;
    private String nombrePlan;
    private BigDecimal montoTotal;
    private String estadoOrden;
    private Instant fechaEmision;

    public ResumenOrdenSuscripcionDto() {
    }

    public ResumenOrdenSuscripcionDto(UUID idOrden, String numeroOrden, String nombrePlan,
                                      BigDecimal montoTotal, String estadoOrden, Instant fechaEmision) {
        this.idOrden = idOrden;
        this.numeroOrden = numeroOrden;
        this.nombrePlan = nombrePlan;
        this.montoTotal = montoTotal;
        this.estadoOrden = estadoOrden;
        this.fechaEmision = fechaEmision;
    }

    public UUID getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(String estadoOrden) {
        this.estadoOrden = estadoOrden;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idOrden;
        private String numeroOrden;
        private String nombrePlan;
        private BigDecimal montoTotal;
        private String estadoOrden;
        private Instant fechaEmision;

        public Constructor idOrden(UUID idOrden) {
            this.idOrden = idOrden;
            return this;
        }

        public Constructor numeroOrden(String numeroOrden) {
            this.numeroOrden = numeroOrden;
            return this;
        }

        public Constructor nombrePlan(String nombrePlan) {
            this.nombrePlan = nombrePlan;
            return this;
        }

        public Constructor montoTotal(BigDecimal montoTotal) {
            this.montoTotal = montoTotal;
            return this;
        }

        public Constructor estadoOrden(String estadoOrden) {
            this.estadoOrden = estadoOrden;
            return this;
        }

        public Constructor fechaEmision(Instant fechaEmision) {
            this.fechaEmision = fechaEmision;
            return this;
        }

        public ResumenOrdenSuscripcionDto build() {
            return new ResumenOrdenSuscripcionDto(idOrden, numeroOrden, nombrePlan, montoTotal, estadoOrden, fechaEmision);
        }
    }
}
