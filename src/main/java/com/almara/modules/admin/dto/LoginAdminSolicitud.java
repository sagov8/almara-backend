package com.almara.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de inicio de sesión para el usuario Administrador (HU-11).
 */
public class LoginAdminSolicitud {

    @NotBlank(message = "El usuario o correo del administrador es obligatorio")
    private String usuarioAdmin;

    @NotBlank(message = "La contraseña es obligatoria")
    private String passwordAdmin;

    public LoginAdminSolicitud() {
    }

    public LoginAdminSolicitud(String usuarioAdmin, String passwordAdmin) {
        this.usuarioAdmin = usuarioAdmin;
        this.passwordAdmin = passwordAdmin;
    }

    public String getUsuarioAdmin() {
        return usuarioAdmin;
    }

    public void setUsuarioAdmin(String usuarioAdmin) {
        this.usuarioAdmin = usuarioAdmin;
    }

    public String getPasswordAdmin() {
        return passwordAdmin;
    }

    public void setPasswordAdmin(String passwordAdmin) {
        this.passwordAdmin = passwordAdmin;
    }
}
