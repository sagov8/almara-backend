package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para el procesamiento o reintento de pago de suscripción SaaS (HU-23).
 */
public class PagoSuscripcionRespuesta {

    private UUID idPago;
    private UUID idOrden;
    private String numeroOrden;
    private String referenciaExterna;
    private BigDecimal montoPagado;
    private String metodoPago;
    private String estadoPago;
    private String motivoRechazo;
    private boolean pagoAprobado;
    private boolean permiteReintento;
    private String estadoOrden;
    private String codigoPlanActivado;
    private String estadoSuscripcion;
    private Instant nuevaFechaExpiracion;
    private boolean notificacionEnviada;
    private String tokenAccesoActualizado;
    private String mensaje;

    public PagoSuscripcionRespuesta() {
    }

    public PagoSuscripcionRespuesta(UUID idPago, UUID idOrden, String numeroOrden, String referenciaExterna,
                                    BigDecimal montoPagado, String metodoPago, String estadoPago, String motivoRechazo,
                                    boolean pagoAprobado, boolean permiteReintento, String estadoOrden,
                                    String codigoPlanActivado, String estadoSuscripcion, Instant nuevaFechaExpiracion,
                                    boolean notificacionEnviada, String tokenAccesoActualizado, String mensaje) {
        this.idPago = idPago;
        this.idOrden = idOrden;
        this.numeroOrden = numeroOrden;
        this.referenciaExterna = referenciaExterna;
        this.montoPagado = montoPagado;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.motivoRechazo = motivoRechazo;
        this.pagoAprobado = pagoAprobado;
        this.permiteReintento = permiteReintento;
        this.estadoOrden = estadoOrden;
        this.codigoPlanActivado = codigoPlanActivado;
        this.estadoSuscripcion = estadoSuscripcion;
        this.nuevaFechaExpiracion = nuevaFechaExpiracion;
        this.notificacionEnviada = notificacionEnviada;
        this.tokenAccesoActualizado = tokenAccesoActualizado;
        this.mensaje = mensaje;
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

    public String getNumeroOrden() {
        return numeroOrden;
    }

    public void setNumeroOrden(String numeroOrden) {
        this.numeroOrden = numeroOrden;
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

    public boolean isPagoAprobado() {
        return pagoAprobado;
    }

    public void setPagoAprobado(boolean pagoAprobado) {
        this.pagoAprobado = pagoAprobado;
    }

    public boolean isPermiteReintento() {
        return permiteReintento;
    }

    public void setPermiteReintento(boolean permiteReintento) {
        this.permiteReintento = permiteReintento;
    }

    public String getEstadoOrden() {
        return estadoOrden;
    }

    public void setEstadoOrden(String estadoOrden) {
        this.estadoOrden = estadoOrden;
    }

    public String getCodigoPlanActivado() {
        return codigoPlanActivado;
    }

    public void setCodigoPlanActivado(String codigoPlanActivado) {
        this.codigoPlanActivado = codigoPlanActivado;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public Instant getNuevaFechaExpiracion() {
        return nuevaFechaExpiracion;
    }

    public void setNuevaFechaExpiracion(Instant nuevaFechaExpiracion) {
        this.nuevaFechaExpiracion = nuevaFechaExpiracion;
    }

    public boolean isNotificacionEnviada() {
        return notificacionEnviada;
    }

    public void setNotificacionEnviada(boolean notificacionEnviada) {
        this.notificacionEnviada = notificacionEnviada;
    }

    public String getTokenAccesoActualizado() {
        return tokenAccesoActualizado;
    }

    public void setTokenAccesoActualizado(String tokenAccesoActualizado) {
        this.tokenAccesoActualizado = tokenAccesoActualizado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idPago;
        private UUID idOrden;
        private String numeroOrden;
        private String referenciaExterna;
        private BigDecimal montoPagado = BigDecimal.ZERO;
        private String metodoPago;
        private String estadoPago;
        private String motivoRechazo;
        private boolean pagoAprobado;
        private boolean permiteReintento;
        private String estadoOrden;
        private String codigoPlanActivado;
        private String estadoSuscripcion;
        private Instant nuevaFechaExpiracion;
        private boolean notificacionEnviada;
        private String tokenAccesoActualizado;
        private String mensaje;

        public Constructor idPago(UUID idPago) {
            this.idPago = idPago;
            return this;
        }

        public Constructor idOrden(UUID idOrden) {
            this.idOrden = idOrden;
            return this;
        }

        public Constructor numeroOrden(String numeroOrden) {
            this.numeroOrden = numeroOrden;
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

        public Constructor pagoAprobado(boolean pagoAprobado) {
            this.pagoAprobado = pagoAprobado;
            return this;
        }

        public Constructor permiteReintento(boolean permiteReintento) {
            this.permiteReintento = permiteReintento;
            return this;
        }

        public Constructor estadoOrden(String estadoOrden) {
            this.estadoOrden = estadoOrden;
            return this;
        }

        public Constructor codigoPlanActivado(String codigoPlanActivado) {
            this.codigoPlanActivado = codigoPlanActivado;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor nuevaFechaExpiracion(Instant nuevaFechaExpiracion) {
            this.nuevaFechaExpiracion = nuevaFechaExpiracion;
            return this;
        }

        public Constructor notificacionEnviada(boolean notificacionEnviada) {
            this.notificacionEnviada = notificacionEnviada;
            return this;
        }

        public Constructor tokenAccesoActualizado(String tokenAccesoActualizado) {
            this.tokenAccesoActualizado = tokenAccesoActualizado;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public PagoSuscripcionRespuesta build() {
            return new PagoSuscripcionRespuesta(
                    idPago, idOrden, numeroOrden, referenciaExterna,
                    montoPagado, metodoPago, estadoPago, motivoRechazo,
                    pagoAprobado, permiteReintento, estadoOrden,
                    codigoPlanActivado, estadoSuscripcion, nuevaFechaExpiracion,
                    notificacionEnviada, tokenAccesoActualizado, mensaje
            );
        }
    }
}
