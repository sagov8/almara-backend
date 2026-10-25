package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Solicitud administrativa para aprobar una cuenta institucional asignándole un plan (HU-15).
 */
public class AprobarInstitucionSolicitud {

    private UUID idPlan;
    private String codigoPlan;

    @Size(max = 255, message = "El motivo no puede superar los 255 caracteres")
    private String motivo;

    public AprobarInstitucionSolicitud() {
    }

    public AprobarInstitucionSolicitud(UUID idPlan, String codigoPlan, String motivo) {
        this.idPlan = idPlan;
        this.codigoPlan = codigoPlan;
        this.motivo = motivo;
    }

    public UUID getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(UUID idPlan) {
        this.idPlan = idPlan;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
