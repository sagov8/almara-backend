package com.almara.modules.institutional.dto;

import java.time.Instant;

/**
 * DTO de respuesta para la operación de Cierre de Sesión y Revocación de Token (HU-18).
 */
public class LogoutInstitucionalRespuesta {

    private String mensaje;
    private boolean tokenRevocado;
    private Instant marcaTemporal;

    public LogoutInstitucionalRespuesta() {
    }

    public LogoutInstitucionalRespuesta(String mensaje, boolean tokenRevocado, Instant marcaTemporal) {
        this.mensaje = mensaje;
        this.tokenRevocado = tokenRevocado;
        this.marcaTemporal = marcaTemporal;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isTokenRevocado() {
        return tokenRevocado;
    }

    public void setTokenRevocado(boolean tokenRevocado) {
        this.tokenRevocado = tokenRevocado;
    }

    public Instant getMarcaTemporal() {
        return marcaTemporal;
    }

    public void setMarcaTemporal(Instant marcaTemporal) {
        this.marcaTemporal = marcaTemporal;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String mensaje;
        private boolean tokenRevocado;
        private Instant marcaTemporal;

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Constructor tokenRevocado(boolean tokenRevocado) {
            this.tokenRevocado = tokenRevocado;
            return this;
        }

        public Constructor marcaTemporal(Instant marcaTemporal) {
            this.marcaTemporal = marcaTemporal;
            return this;
        }

        public LogoutInstitucionalRespuesta build() {
            return new LogoutInstitucionalRespuesta(mensaje, tokenRevocado, marcaTemporal);
        }
    }
}
