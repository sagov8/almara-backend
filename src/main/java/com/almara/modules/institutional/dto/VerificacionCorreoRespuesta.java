package com.almara.modules.institutional.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO para la respuesta de verificación de correo y activación de cuenta.
 */
public class VerificacionCorreoRespuesta {

    private UUID idInstitucion;
    private String razonSocial;
    private String correoCorporativo;
    private String estadoCuenta;
    private boolean correoVerificado;
    private UUID idSuscripcion;
    private String codigoPlan;
    private Instant fechaExpiracionSuscripcion;
    private String mensaje;

    public VerificacionCorreoRespuesta() {
    }

    public VerificacionCorreoRespuesta(UUID idInstitucion, String razonSocial, String correoCorporativo,
                                      String estadoCuenta, boolean correoVerificado, UUID idSuscripcion,
                                      String codigoPlan, Instant fechaExpiracionSuscripcion, String mensaje) {
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.correoCorporativo = correoCorporativo;
        this.estadoCuenta = estadoCuenta;
        this.correoVerificado = correoVerificado;
        this.idSuscripcion = idSuscripcion;
        this.codigoPlan = codigoPlan;
        this.fechaExpiracionSuscripcion = fechaExpiracionSuscripcion;
        this.mensaje = mensaje;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCorreoCorporativo() {
        return correoCorporativo;
    }

    public void setCorreoCorporativo(String correoCorporativo) {
        this.correoCorporativo = correoCorporativo;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public UUID getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(UUID idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public Instant getFechaExpiracionSuscripcion() {
        return fechaExpiracionSuscripcion;
    }

    public void setFechaExpiracionSuscripcion(Instant fechaExpiracionSuscripcion) {
        this.fechaExpiracionSuscripcion = fechaExpiracionSuscripcion;
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
        private UUID idInstitucion;
        private String razonSocial;
        private String correoCorporativo;
        private String estadoCuenta;
        private boolean correoVerificado;
        private UUID idSuscripcion;
        private String codigoPlan;
        private Instant fechaExpiracionSuscripcion;
        private String mensaje;

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor correoCorporativo(String correoCorporativo) {
            this.correoCorporativo = correoCorporativo;
            return this;
        }

        public Constructor estadoCuenta(String estadoCuenta) {
            this.estadoCuenta = estadoCuenta;
            return this;
        }

        public Constructor correoVerificado(boolean correoVerificado) {
            this.correoVerificado = correoVerificado;
            return this;
        }

        public Constructor idSuscripcion(UUID idSuscripcion) {
            this.idSuscripcion = idSuscripcion;
            return this;
        }

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor fechaExpiracionSuscripcion(Instant fechaExpiracionSuscripcion) {
            this.fechaExpiracionSuscripcion = fechaExpiracionSuscripcion;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public VerificacionCorreoRespuesta build() {
            return new VerificacionCorreoRespuesta(idInstitucion, razonSocial, correoCorporativo,
                    estadoCuenta, correoVerificado, idSuscripcion, codigoPlan,
                    fechaExpiracionSuscripcion, mensaje);
        }
    }
}
