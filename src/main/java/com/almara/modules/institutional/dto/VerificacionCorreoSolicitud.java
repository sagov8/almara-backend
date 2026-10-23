package com.almara.modules.institutional.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de verificación de correo institucional.
 */
public class VerificacionCorreoSolicitud {

    @NotBlank(message = "El token de verificación es obligatorio")
    private String tokenVerificacion;

    public VerificacionCorreoSolicitud() {
    }

    public VerificacionCorreoSolicitud(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public String getTokenVerificacion() {
        return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String tokenVerificacion;

        public Constructor tokenVerificacion(String tokenVerificacion) {
            this.tokenVerificacion = tokenVerificacion;
            return this;
        }

        public VerificacionCorreoSolicitud build() {
            return new VerificacionCorreoSolicitud(tokenVerificacion);
        }
    }
}
