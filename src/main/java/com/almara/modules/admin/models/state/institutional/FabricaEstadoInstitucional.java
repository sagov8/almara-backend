package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.EstadoCuentaInstitucional;

import java.util.Map;

/**
 * Fábrica para instanciar u obtener el objeto de estado State correspondiente a la cuenta institucional.
 */
public final class FabricaEstadoInstitucional {

    private static final EstadoCuentaInstitucionalState PENDIENTE = new EstadoPendienteAprobacionState();
    private static final EstadoCuentaInstitucionalState ACTIVA = new EstadoActivaState();
    private static final EstadoCuentaInstitucionalState SUSPENDIDA = new EstadoSuspendidaState();
    private static final EstadoCuentaInstitucionalState INACTIVA = new EstadoInactivaState();

    private static final Map<String, EstadoCuentaInstitucionalState> MAPA_ESTADOS = Map.of(
            EstadoCuentaInstitucional.PENDIENTE_APROBACION.name(), PENDIENTE,
            EstadoCuentaInstitucional.ACTIVA.name(), ACTIVA,
            EstadoCuentaInstitucional.SUSPENDIDA.name(), SUSPENDIDA,
            EstadoCuentaInstitucional.INACTIVA.name(), INACTIVA
    );

    private FabricaEstadoInstitucional() {
    }

    public static EstadoCuentaInstitucionalState obtenerEstado(String codigoEstado) {
        if (codigoEstado == null || codigoEstado.isBlank()) {
            return PENDIENTE;
        }
        EstadoCuentaInstitucionalState estado = MAPA_ESTADOS.get(codigoEstado.trim().toUpperCase());
        if (estado == null) {
            throw new IllegalArgumentException("Estado de cuenta institucional no reconocido: " + codigoEstado);
        }
        return estado;
    }
}
