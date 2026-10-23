package com.almara.modules.institutional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para la solicitud de inicio de sesión institucional (HU-17).
 */
public class LoginInstitucionalSolicitud {

    @NotBlank(message = "El correo corporativo es obligatorio")
    @Email(message = "El formato de correo no es válido")
    private String correoInstitucional;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public LoginInstitucionalSolicitud() {
    }

    public LoginInstitucionalSolicitud(String correoInstitucional, String password) {
        this.correoInstitucional = correoInstitucional;
        this.password = password;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String correoInstitucional;
        private String password;

        public Constructor correoInstitucional(String correoInstitucional) {
            this.correoInstitucional = correoInstitucional;
            return this;
        }

        public Constructor password(String password) {
            this.password = password;
            return this;
        }

        public LoginInstitucionalSolicitud build() {
            return new LoginInstitucionalSolicitud(correoInstitucional, password);
        }
    }
}
