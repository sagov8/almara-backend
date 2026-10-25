package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Size;

/**
 * Solicitud administrativa para reactivar una cuenta institucional (HU-15).
 */
public class ReactivarInstitucionSolicitud {

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    public ReactivarInstitucionSolicitud() {
    }

    public ReactivarInstitucionSolicitud(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
