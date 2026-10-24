package com.almara.modules.admin.dto;

/**
 * Respuesta del cierre de sesión administrativo (HU-18).
 */
public class LogoutAdminRespuesta {

    private boolean exitoso;
    private String mensaje;

    public LogoutAdminRespuesta() {
    }

    public LogoutAdminRespuesta(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
