package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;

/**
 * Estado concreto: INACTIVA.
 * La entidad ha sido dada de baja (cancelación de contrato, retiro formal).
 */
public class EstadoInactivaState implements EstadoCuentaInstitucionalState {

    @Override
    public String getCodigoEstado() {
        return EstadoCuentaInstitucional.INACTIVA.name();
    }

    @Override
    public void aprobar(ClienteInstitucional cliente, PlanSuscripcion plan, String motivo) {
        throw new IllegalStateException("Una cuenta inactiva o dada de baja no puede ser aprobada directamente. Debe reactivarse.");
    }

    @Override
    public void suspender(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("No se puede suspender una cuenta que ya está inactiva o dada de baja.");
    }

    @Override
    public void darDeBaja(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("La cuenta institucional ya ha sido dada de baja previamente.");
    }

    @Override
    public void reactivar(ClienteInstitucional cliente, String motivo) {
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.ACTIVA.name());
        cliente.setActivo(true);
    }
}
