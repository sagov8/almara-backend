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
 * Entidad de dominio Línea de Detalle de una Orden de Pedido (HU-22).
 * Representa la composición fuerte: OrdenPedido *-- LineaDetalle.
 */
@Entity
@Table(name = "linea_detalle")
public class LineaDetalle {

    @Id
    @Column(name = "id_linea")
    private UUID idLinea;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenPedido ordenPedido;

    @Column(name = "id_producto", nullable = false)
    private UUID idProducto;

    @Column(name = "concepto", length = 150, nullable = false)
    private String concepto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad = 1;

    @Column(name = "precio_aplicado", precision = 12, scale = 2, nullable = false)
    private BigDecimal precioAplicado;

    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    public LineaDetalle() {
    }

    public LineaDetalle(UUID idLinea, OrdenPedido ordenPedido, UUID idProducto, String concepto,
                        int cantidad, BigDecimal precioAplicado, BigDecimal subtotal) {
        this.idLinea = idLinea;
        this.ordenPedido = ordenPedido;
        this.idProducto = idProducto;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioAplicado = precioAplicado;
        this.subtotal = subtotal != null ? subtotal : calcularLinea();
    }

    /**
     * Calcula el subtotal de la línea de detalle según el precio aplicado y la cantidad.
     */
    public BigDecimal calcularLinea() {
        if (precioAplicado == null) {
            return BigDecimal.ZERO;
        }
        return precioAplicado.multiply(BigDecimal.valueOf(Math.max(1, cantidad)));
    }

    public UUID getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(UUID idLinea) {
        this.idLinea = idLinea;
    }

    public OrdenPedido getOrdenPedido() {
        return ordenPedido;
    }

    public void setOrdenPedido(OrdenPedido ordenPedido) {
        this.ordenPedido = ordenPedido;
    }

    public UUID getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(UUID idProducto) {
        this.idProducto = idProducto;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioAplicado() {
        return precioAplicado;
    }

    public void setPrecioAplicado(BigDecimal precioAplicado) {
        this.precioAplicado = precioAplicado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idLinea;
        private OrdenPedido ordenPedido;
        private UUID idProducto;
        private String concepto;
        private int cantidad = 1;
        private BigDecimal precioAplicado = BigDecimal.ZERO;
        private BigDecimal subtotal;

        public Constructor idLinea(UUID idLinea) {
            this.idLinea = idLinea;
            return this;
        }

        public Constructor ordenPedido(OrdenPedido ordenPedido) {
            this.ordenPedido = ordenPedido;
            return this;
        }

        public Constructor idProducto(UUID idProducto) {
            this.idProducto = idProducto;
            return this;
        }

        public Constructor concepto(String concepto) {
            this.concepto = concepto;
            return this;
        }

        public Constructor cantidad(int cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Constructor precioAplicado(BigDecimal precioAplicado) {
            this.precioAplicado = precioAplicado;
            return this;
        }

        public Constructor subtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public LineaDetalle build() {
            BigDecimal subtotalCalculado = subtotal != null ? subtotal :
                    (precioAplicado != null ? precioAplicado.multiply(BigDecimal.valueOf(Math.max(1, cantidad))) : BigDecimal.ZERO);
            return new LineaDetalle(idLinea, ordenPedido, idProducto, concepto, cantidad, precioAplicado, subtotalCalculado);
        }
    }
}
