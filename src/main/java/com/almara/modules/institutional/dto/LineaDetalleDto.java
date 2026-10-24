package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para la representación de una línea de detalle de la orden de pedido (HU-22).
 */
public class LineaDetalleDto {

    private UUID idLinea;
    private String concepto;
    private int cantidad;
    private BigDecimal precioAplicado;
    private BigDecimal subtotal;

    public LineaDetalleDto() {
    }

    public LineaDetalleDto(UUID idLinea, String concepto, int cantidad, BigDecimal precioAplicado, BigDecimal subtotal) {
        this.idLinea = idLinea;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioAplicado = precioAplicado;
        this.subtotal = subtotal;
    }

    public UUID getIdLinea() {
        return idLinea;
    }

    public void setIdLinea(UUID idLinea) {
        this.idLinea = idLinea;
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
        private String concepto;
        private int cantidad;
        private BigDecimal precioAplicado;
        private BigDecimal subtotal;

        public Constructor idLinea(UUID idLinea) {
            this.idLinea = idLinea;
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

        public LineaDetalleDto build() {
            return new LineaDetalleDto(idLinea, concepto, cantidad, precioAplicado, subtotal);
        }
    }
}
