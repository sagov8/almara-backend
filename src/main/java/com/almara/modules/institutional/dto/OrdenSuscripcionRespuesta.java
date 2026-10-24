package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para la Orden de Suscripción generada durante el checkout (HU-22).
 */
public class OrdenSuscripcionRespuesta {

    private UUID idOrden;
    private String numeroOrden;
    private UUID idInstitucion;
    private UUID idPlanSeleccionado;
    private String codigoPlan;
    private String nombrePlan;
    private int duracionDias;
    private Instant fechaEmision;
    private String estadoOrden;
    private BigDecimal montoTotal;
    private List<LineaDetalleDto> lineasDetalle = new ArrayList<>();
    private String siguientePaso = "PROCESAR_PAGO";

    public OrdenSuscripcionRespuesta() {
    }

    public OrdenSuscripcionRespuesta(UUID idOrden, String numeroOrden, UUID idInstitucion, UUID idPlanSeleccionado,
                                     String codigoPlan, String nombrePlan, int duracionDias, Instant fechaEmision,
                                     String estadoOrden, BigDecimal montoTotal, List<LineaDetalleDto> lineasDetalle,
                                     String siguientePaso) {
        this.idOrden = idOrden;
        this.numeroOrden = numeroOrden;
        this.idInstitucion = idInstitucion;
        this.idPlanSeleccionado = idPlanSeleccionado;
        this.codigoPlan = codigoPlan;
        this.nombrePlan = nombrePlan;
        this.duracionDias = duracionDias;
        this.fechaEmision = fechaEmision;
        this.estadoOrden = estadoOrden;
        this.montoTotal = montoTotal;
        this.lineasDetalle = lineasDetalle != null ? lineasDetalle : new ArrayList<>();
        this.siguientePaso = siguientePaso != null ? siguientePaso : "PROCESAR_PAGO";
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

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public UUID getIdPlanSeleccionado() {
        return idPlanSeleccionado;
    }

    public void setIdPlanSeleccionado(UUID idPlanSeleccionado) {
        this.idPlanSeleccionado = idPlanSeleccionado;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(String estadoOrden) {
        this.estadoOrden = estadoOrden;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public List<LineaDetalleDto> getLineasDetalle() {
        return lineasDetalle;
    }

    public void setLineasDetalle(List<LineaDetalleDto> lineasDetalle) {
        this.lineasDetalle = lineasDetalle;
    }

    public String getSiguientePaso() {
        return siguientePaso;
    }

    public void setSiguientePaso(String siguientePaso) {
        this.siguientePaso = siguientePaso;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idOrden;
        private String numeroOrden;
        private UUID idInstitucion;
        private UUID idPlanSeleccionado;
        private String codigoPlan;
        private String nombrePlan;
        private int duracionDias;
        private Instant fechaEmision;
        private String estadoOrden;
        private BigDecimal montoTotal;
        private List<LineaDetalleDto> lineasDetalle = new ArrayList<>();
        private String siguientePaso = "PROCESAR_PAGO";

        public Constructor idOrden(UUID idOrden) {
            this.idOrden = idOrden;
            return this;
        }

        public Constructor numeroOrden(String numeroOrden) {
            this.numeroOrden = numeroOrden;
            return this;
        }

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor idPlanSeleccionado(UUID idPlanSeleccionado) {
            this.idPlanSeleccionado = idPlanSeleccionado;
            return this;
        }

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor nombrePlan(String nombrePlan) {
            this.nombrePlan = nombrePlan;
            return this;
        }

        public Constructor duracionDias(int duracionDias) {
            this.duracionDias = duracionDias;
            return this;
        }

        public Constructor fechaEmision(Instant fechaEmision) {
            this.fechaEmision = fechaEmision;
            return this;
        }

        public Constructor estadoOrden(String estadoOrden) {
            this.estadoOrden = estadoOrden;
            return this;
        }

        public Constructor montoTotal(BigDecimal montoTotal) {
            this.montoTotal = montoTotal;
            return this;
        }

        public Constructor lineasDetalle(List<LineaDetalleDto> lineasDetalle) {
            this.lineasDetalle = lineasDetalle;
            return this;
        }

        public Constructor siguientePaso(String siguientePaso) {
            this.siguientePaso = siguientePaso;
            return this;
        }

        public OrdenSuscripcionRespuesta build() {
            return new OrdenSuscripcionRespuesta(
                    idOrden, numeroOrden, idInstitucion, idPlanSeleccionado,
                    codigoPlan, nombrePlan, duracionDias, fechaEmision,
                    estadoOrden, montoTotal, lineasDetalle, siguientePaso
            );
        }
    }
}
