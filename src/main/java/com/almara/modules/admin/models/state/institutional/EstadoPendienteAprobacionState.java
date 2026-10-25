package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;

/**
 * Estado concreto: PENDIENTE_APROBACION.
 * La entidad se registró pero requiere visto bueno administrativo para ingresar y acceder al sistema.
 */
public class EstadoPendienteAprobacionState implements EstadoCuentaInstitucionalState {

    @Override
    public String getCodigoEstado() {
        return EstadoCuentaInstitucional.PENDIENTE_APROBACION.name();
    }

    @Override
    public void aprobar(ClienteInstitucional cliente, PlanSuscripcion plan, String motivo) {
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.ACTIVA.name());
        cliente.setCorreoVerificado(true);
        cliente.setActivo(true);
        cliente.setTokenVerificacion(null);
    }

    @Override
    public void suspender(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("No es posible suspender una cuenta pendiente de aprobación. Debe ser aprobada o dada de baja.");
    }

    @Override
    public void darDeBaja(ClienteInstitucional cliente, String motivo) {
        cliente.setEstadoCuenta(EstadoCuentaInstitucional.INACTIVA.name());
        cliente.setActivo(false);
    }

    @Override
    public void reactivar(ClienteInstitucional cliente, String motivo) {
        throw new IllegalStateException("Una cuenta pendiente de aprobación no se reactiva; debe ser aprobada.");
    }
}
