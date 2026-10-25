package com.almara.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud administrativa para dar de baja definitiva una cuenta institucional (HU-15).
 */
public class DarDeBajaInstitucionSolicitud {

    @NotBlank(message = "El motivo de baja es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String motivo;

    public DarDeBajaInstitucionSolicitud() {
    }

    public DarDeBajaInstitucionSolicitud(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
