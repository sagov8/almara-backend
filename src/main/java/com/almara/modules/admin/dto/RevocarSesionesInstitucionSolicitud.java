package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Size;

/**
 * Solicitud administrativa para revocar tokens y sesiones activas concurrentes (HU-15).
 */
public class RevocarSesionesInstitucionSolicitud {

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    public RevocarSesionesInstitucionSolicitud() {
    }

    public RevocarSesionesInstitucionSolicitud(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
