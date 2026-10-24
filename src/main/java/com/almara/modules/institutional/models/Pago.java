package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio Pago para transacciones de suscripción SaaS (HU-23).
 */
@Entity
@Table(name = "pago_suscripcion")
public class Pago {

    @Id
    @Column(name = "id_pago")
    private UUID idPago;

    @Column(name = "id_orden", nullable = false)
    private UUID idOrden;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Column(name = "monto_pagado", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoPagado;

    @Column(name = "metodo_pago", length = 40, nullable = false)
    private String metodoPago;

    @Column(name = "estado_pago", length = 25, nullable = false)
    private String estadoPago;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @Column(name = "fecha_transaccion", nullable = false)
    private Instant fechaTransaccion = Instant.now();

    public Pago() {
    }

    public Pago(UUID idPago, UUID idOrden, String referenciaExterna, BigDecimal montoPagado,
                String metodoPago, String estadoPago, String motivoRechazo, Instant fechaTransaccion) {
        this.idPago = idPago;
        this.idOrden = idOrden;
        this.referenciaExterna = referenciaExterna;
        this.montoPagado = montoPagado;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.motivoRechazo = motivoRechazo;
        this.fechaTransaccion = fechaTransaccion != null ? fechaTransaccion : Instant.now();
    }

    /**
     * Valida si la transacción de pago fue aprobada exitosamente según el UML.
     */
    public boolean procesarTransaccion() {
        return "APROBADO".equalsIgnoreCase(this.estadoPago);
    }

    /**
     * Registra el rechazo de la transacción con su motivo.
     */
    public void registrarRechazo(String motivo) {
        this.estadoPago = "RECHAZADO";
        this.motivoRechazo = motivo;
    }

    public UUID getIdPago() {
        return idPago;
    }

    public void setIdPago(UUID idPago) {
        this.idPago = idPago;
    }

    public UUID getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public String getReferenciaExterna() {
        return referenciaExterna;
    }

    public void setReferenciaExterna(String referenciaExterna) {
        this.referenciaExterna = referenciaExterna;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public Instant getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(Instant fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idPago;
        private UUID idOrden;
        private String referenciaExterna;
        private BigDecimal montoPagado = BigDecimal.ZERO;
        private String metodoPago;
        private String estadoPago = "PENDIENTE";
        private String motivoRechazo;
        private Instant fechaTransaccion = Instant.now();

        public Constructor idPago(UUID idPago) {
            this.idPago = idPago;
            return this;
        }

        public Constructor idOrden(UUID idOrden) {
            this.idOrden = idOrden;
            return this;
        }

        public Constructor referenciaExterna(String referenciaExterna) {
            this.referenciaExterna = referenciaExterna;
            return this;
        }

        public Constructor montoPagado(BigDecimal montoPagado) {
            this.montoPagado = montoPagado;
            return this;
        }

        public Constructor metodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
            return this;
        }

        public Constructor estadoPago(String estadoPago) {
            this.estadoPago = estadoPago;
            return this;
        }

        public Constructor motivoRechazo(String motivoRechazo) {
            this.motivoRechazo = motivoRechazo;
            return this;
        }

        public Constructor fechaTransaccion(Instant fechaTransaccion) {
            this.fechaTransaccion = fechaTransaccion;
            return this;
        }

        public Pago build() {
            return new Pago(idPago, idOrden, referenciaExterna, montoPagado, metodoPago, estadoPago, motivoRechazo, fechaTransaccion);
        }
    }
}
