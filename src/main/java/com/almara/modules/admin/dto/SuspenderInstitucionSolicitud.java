package com.almara.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud administrativa para suspender una cuenta institucional (HU-15).
 */
public class SuspenderInstitucionSolicitud {

    @NotBlank(message = "El motivo de suspensión es obligatorio")
    @Size(min = 5, max = 255, message = "El motivo debe tener entre 5 y 255 caracteres")
    private String motivo;

    public SuspenderInstitucionSolicitud() {
    }

    public SuspenderInstitucionSolicitud(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
