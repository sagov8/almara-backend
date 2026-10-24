package com.almara.modules.admin.dto;

import java.util.UUID;

/**
 * Respuesta exitosa de autenticación para el usuario Administrador (HU-11).
 */
public class LoginAdminRespuesta {

    private String token;
    private String tipoToken = "Bearer";
    private UUID idAdmin;
    private String nombreCompleto;
    private String correo;
    private String rol = "ROLE_ADMIN";
    private long expiraEnSegundos;

    public LoginAdminRespuesta() {
    }

    public LoginAdminRespuesta(String token, UUID idAdmin, String nombreCompleto,
                               String correo, String rol, long expiraEnSegundos) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.idAdmin = idAdmin;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol != null ? rol : "ROLE_ADMIN";
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public UUID getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(UUID idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public void setExpiraEnSegundos(long expiraEnSegundos) {
        this.expiraEnSegundos = expiraEnSegundos;
    }
}
