package com.almara.modules.institutional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Solicitud para procesar o reintentar el pago de una orden de suscripción SaaS (HU-23).
 */
public class ProcesarPagoSolicitud {

    @NotNull(message = "El identificador de la orden es obligatorio")
    private UUID idOrden;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @NotBlank(message = "El token o identificador de tarjeta/fuente de pago es obligatorio")
    private String tokenMedioPago;

    public ProcesarPagoSolicitud() {
    }

    public ProcesarPagoSolicitud(UUID idOrden, String metodoPago, String tokenMedioPago) {
        this.idOrden = idOrden;
        this.metodoPago = metodoPago;
        this.tokenMedioPago = tokenMedioPago;
    }

    public UUID getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(UUID idOrden) {
        this.idOrden = idOrden;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getTokenMedioPago() {
        return tokenMedioPago;
    }

    public void setTokenMedioPago(String tokenMedioPago) {
        this.tokenMedioPago = tokenMedioPago;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idOrden;
        private String metodoPago = "TARJETA_CREDITO";
        private String tokenMedioPago;

        public Constructor idOrden(UUID idOrden) {
            this.idOrden = idOrden;
            return this;
        }

        public Constructor metodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
            return this;
        }

        public Constructor tokenMedioPago(String tokenMedioPago) {
            this.tokenMedioPago = tokenMedioPago;
            return this;
        }

        public ProcesarPagoSolicitud build() {
            return new ProcesarPagoSolicitud(idOrden, metodoPago, tokenMedioPago);
        }
    }
}
