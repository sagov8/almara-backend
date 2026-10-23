package com.almara.modules.institutional.dto;

import java.util.UUID;

/**
 * DTO para la respuesta de registro de cliente institucional (HU-16).
 */
public class RegistroInstitucionalRespuesta {

    private UUID idInstitucion;
    private String razonSocial;
    private String correoCorporativo;
    private String estadoCuenta;
    private boolean correoVerificado;
    private String tokenVerificacionSimulado;
    private String mensaje;

    public RegistroInstitucionalRespuesta() {
    }

    public RegistroInstitucionalRespuesta(UUID idInstitucion, String razonSocial, String correoCorporativo,
                                          String estadoCuenta, boolean correoVerificado,
                                          String tokenVerificacionSimulado, String mensaje) {
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.correoCorporativo = correoCorporativo;
        this.estadoCuenta = estadoCuenta;
        this.correoVerificado = correoVerificado;
        this.tokenVerificacionSimulado = tokenVerificacionSimulado;
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

    public String getTokenVerificacionSimulado() {
        return tokenVerificacionSimulado;
    }

    public void setTokenVerificacionSimulado(String tokenVerificacionSimulado) {
        this.tokenVerificacionSimulado = tokenVerificacionSimulado;
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
        private String tokenVerificacionSimulado;
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

        public Constructor tokenVerificacionSimulado(String tokenVerificacionSimulado) {
            this.tokenVerificacionSimulado = tokenVerificacionSimulado;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public RegistroInstitucionalRespuesta build() {
            return new RegistroInstitucionalRespuesta(idInstitucion, razonSocial, correoCorporativo,
                    estadoCuenta, correoVerificado, tokenVerificacionSimulado, mensaje);
        }
    }
}
