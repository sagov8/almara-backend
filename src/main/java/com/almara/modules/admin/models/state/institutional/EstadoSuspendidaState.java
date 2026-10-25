package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;

/**
 * Estado concreto: SUSPENDIDA.
 * La entidad ha sido pausada temporalmente (por impago, revisión contractual o solicitud preventiva).
 */
public class EstadoSuspendidaState implements EstadoCuentaInstitucionalState {

    @Override
    public String getCodigoEstado() {
        return EstadoCuentaInstitucional.SUSPENDIDA.name();
    }

    @Override
    public void aprobar(ClienteInstitucional cliente, PlanSuscripcion plan, String motivo) {
        throw new IllegalStateException("Una cuenta suspendida no se aprueba; debe ser reactivada.");
    }

    @Override
    public void suspender(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("La cuenta institucional ya se encuentra suspendida.");
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
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.ACTIVA.name());
        cliente.setActivo(true);
    }
}
