package com.almara.modules.admin.dto;

import java.util.UUID;

/**
 * Respuesta con los datos de sesión activa del Administrador (HU-11, HU-18).
 */
public class SesionAdminRespuesta {

    private boolean autenticado;
    private UUID idAdmin;
    private String nombreCompleto;
    private String correo;
    private String rol;
    private long expiraEnSegundos;

    public SesionAdminRespuesta() {
    }

    public SesionAdminRespuesta(boolean autenticado, UUID idAdmin, String nombreCompleto,
                                String correo, String rol, long expiraEnSegundos) {
        this.autenticado = autenticado;
        this.idAdmin = idAdmin;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
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
