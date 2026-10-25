package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;

/**
 * Estado concreto: ACTIVA.
 * La entidad dispone de acceso regular al panel institucional y su suscripción está operativa.
 */
public class EstadoActivaState implements EstadoCuentaInstitucionalState {

    @Override
    public String getCodigoEstado() {
        return EstadoCuentaInstitucional.ACTIVA.name();
    }

    @Override
    public void aprobar(ClienteInstitucional cliente, PlanSuscripcion plan, String motivo) {
        throw new IllegalStateException("La cuenta institucional ya se encuentra activa.");
    }

    @Override
    public void suspender(ClienteInstitucional cliente, String motivo) {
        if (motivo == null || motivo.trim().isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo o justificación técnica para suspender la cuenta.");
        }
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.SUSPENDIDA.name());
    }

    @Override
    public void darDeBaja(ClienteInstitucional cliente, String motivo) {
        if (motivo == null || motivo.trim().isBlank()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo o justificación técnica para dar de baja la cuenta.");
        }
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.INACTIVA.name());
        cliente.setActivo(false);
    }

    @Override
    public void reactivar(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("La cuenta institucional ya se encuentra en estado activa.");
    }
}
