package com.almara.modules.institutional.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio Orden de Pedido para transacciones y suscripciones SaaS (HU-21, HU-22, HU-23).
 * Modela la composición fuerte: OrdenPedido "1" *-- "1..*" LineaDetalle.
 */
@Entity
@Table(name = "orden_pedido")
public class OrdenPedido {

    @Id
    @Column(name = "id_orden")
    private UUID idOrden;

    @Column(name = "numero_orden", length = 40, nullable = false, unique = true)
    private String numeroOrden;

    @Column(name = "id_institucion", nullable = false)
    private UUID idInstitucion;

    @Column(name = "id_plan_seleccionado", nullable = false)
    private UUID idPlanSeleccionado;

    @Column(name = "fecha_emision", nullable = false)
    private Instant fechaEmision = Instant.now();

    @Column(name = "estado_orden", length = 25, nullable = false)
    private String estadoOrden;

    @Column(name = "monto_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoTotal;

    @OneToMany(mappedBy = "ordenPedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LineaDetalle> lineasDetalle = new ArrayList<>();

    public OrdenPedido() {
    }

    public OrdenPedido(UUID idOrden, String numeroOrden, UUID idInstitucion, UUID idPlanSeleccionado,
                       Instant fechaEmision, String estadoOrden, BigDecimal montoTotal) {
        this.idOrden = idOrden;
        this.numeroOrden = numeroOrden;
        this.idInstitucion = idInstitucion;
        this.idPlanSeleccionado = idPlanSeleccionado;
        this.fechaEmision = fechaEmision != null ? fechaEmision : Instant.now();
        this.estadoOrden = estadoOrden;
        this.montoTotal = montoTotal;
        this.lineasDetalle = new ArrayList<>();
    }

    public OrdenPedido(UUID idOrden, String numeroOrden, UUID idInstitucion, UUID idPlanSeleccionado,
                       Instant fechaEmision, String estadoOrden, BigDecimal montoTotal, List<LineaDetalle> lineasDetalle) {
        this.idOrden = idOrden;
        this.numeroOrden = numeroOrden;
        this.idInstitucion = idInstitucion;
        this.idPlanSeleccionado = idPlanSeleccionado;
        this.fechaEmision = fechaEmision != null ? fechaEmision : Instant.now();
        this.estadoOrden = estadoOrden;
        this.montoTotal = montoTotal;
        this.lineasDetalle = lineasDetalle != null ? lineasDetalle : new ArrayList<>();
    }

    /**
     * Agrega una línea de detalle asegurando la relación bidireccional de composición fuerte.
     */
    public void agregarLineaDetalle(LineaDetalle linea) {
        if (this.lineasDetalle == null) {
            this.lineasDetalle = new ArrayList<>();
        }
        linea.setOrdenPedido(this);
        this.lineasDetalle.add(linea);
        calcularTotal();
    }

    /**
     * Elimina una línea de detalle.
     */
    public void eliminarLineaDetalle(UUID idLinea) {
        if (this.lineasDetalle != null) {
            this.lineasDetalle.removeIf(l -> l.getIdLinea().equals(idLinea));
            calcularTotal();
        }
    }

    /**
     * Recalcula el monto total de la orden sumando los subtotales de sus líneas de detalle.
     */
    public BigDecimal calcularTotal() {
        if (this.lineasDetalle == null || this.lineasDetalle.isEmpty()) {
            return this.montoTotal != null ? this.montoTotal : BigDecimal.ZERO;
        }
        this.montoTotal = this.lineasDetalle.stream()
                .map(LineaDetalle::calcularLinea)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return this.montoTotal;
    }

    /**
     * Confirma el checkout de la orden estableciendo su estado inicial pendiente de pago.
     */
    public void confirmarCheckout() {
        this.estadoOrden = "PENDIENTE_PAGO";
        calcularTotal();
    }

    /**
     * Actualiza el estado del ciclo de vida de la orden (PENDIENTE_PAGO, PAGADA, FALLIDA, CANCELADA).
     */
    public void actualizarEstado(String nuevoEstado) {
        this.estadoOrden = nuevoEstado;
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

    public List<LineaDetalle> getLineasDetalle() {
        return lineasDetalle;
    }

    public void setLineasDetalle(List<LineaDetalle> lineasDetalle) {
        this.lineasDetalle = lineasDetalle;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idOrden;
        private String numeroOrden;
        private UUID idInstitucion;
        private UUID idPlanSeleccionado;
        private Instant fechaEmision = Instant.now();
        private String estadoOrden = "PENDIENTE_PAGO";
        private BigDecimal montoTotal = BigDecimal.ZERO;
        private List<LineaDetalle> lineasDetalle = new ArrayList<>();

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

        public Constructor lineasDetalle(List<LineaDetalle> lineasDetalle) {
            this.lineasDetalle = lineasDetalle;
            return this;
        }

        public OrdenPedido build() {
            OrdenPedido orden = new OrdenPedido(idOrden, numeroOrden, idInstitucion, idPlanSeleccionado, fechaEmision, estadoOrden, montoTotal, lineasDetalle);
            if (lineasDetalle != null && !lineasDetalle.isEmpty()) {
                lineasDetalle.forEach(l -> l.setOrdenPedido(orden));
                if (montoTotal == null || montoTotal.compareTo(BigDecimal.ZERO) == 0) {
                    orden.calcularTotal();
                }
            }
            return orden;
        }
    }
}
