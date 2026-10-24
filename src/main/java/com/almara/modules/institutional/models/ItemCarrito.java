package com.almara.modules.institutional.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entidad de dominio Item de Carrito de Compras SaaS (HU-22).
 */
@Entity
@Table(name = "item_carrito")
public class ItemCarrito {

    @Id
    @Column(name = "id_item")
    private UUID idItem;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito", nullable = false)
    private CarritoCompra carritoCompra;

    @Column(name = "id_plan", nullable = false)
    private UUID idPlan;

    @Column(name = "cantidad", nullable = false)
    private int cantidad = 1;

    @Column(name = "precio_unitario", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioUnitario;

    public ItemCarrito() {
    }

    public ItemCarrito(UUID idItem, CarritoCompra carritoCompra, UUID idPlan, int cantidad, BigDecimal precioUnitario) {
        this.idItem = idItem;
        this.carritoCompra = carritoCompra;
        this.idPlan = idPlan;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    /**
     * Calcula el subtotal del ítem en el carrito según el diagrama de clases.
     */
    public BigDecimal calcularSubtotalItem() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(Math.max(1, cantidad)));
    }

    public UUID getIdItem() {
        return idItem;
    }

    public void setIdItem(UUID idItem) {
        this.idItem = idItem;
    }

    public CarritoCompra getCarritoCompra() {
        return carritoCompra;
    }

    public void setCarritoCompra(CarritoCompra carritoCompra) {
        this.carritoCompra = carritoCompra;
    }

    public UUID getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(UUID idPlan) {
        this.idPlan = idPlan;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idItem;
        private CarritoCompra carritoCompra;
        private UUID idPlan;
        private int cantidad = 1;
        private BigDecimal precioUnitario = BigDecimal.ZERO;

        public Constructor idItem(UUID idItem) {
            this.idItem = idItem;
            return this;
        }

        public Constructor carritoCompra(CarritoCompra carritoCompra) {
            this.carritoCompra = carritoCompra;
            return this;
        }

        public Constructor idPlan(UUID idPlan) {
            this.idPlan = idPlan;
            return this;
        }

        public Constructor cantidad(int cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Constructor precioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
            return this;
        }

        public ItemCarrito build() {
            return new ItemCarrito(idItem, carritoCompra, idPlan, cantidad, precioUnitario);
        }
    }
}
