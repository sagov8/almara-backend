package com.almara.modules.admin.models.state.institutional;

import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;

/**
 * Interfaz del Patrón Estado (State Pattern) para el ciclo de vida de cuentas institucionales (HU-15).
 */
public interface EstadoCuentaInstitucionalState {

    /**
     * Retorna el nombre o código normalizado del estado actual.
     */
    String getCodigoEstado();

    /**
     * Transiciona la cuenta institucional al estado ACTIVA asociándole un plan de suscripción.
     */
    void aprobar(ClienteInstitucional cliente, PlanSuscripcion plan, String motivo);

    /**
     * Transiciona temporalmente la cuenta al estado SUSPENDIDA.
     */
    void suspender(ClienteInstitucional cliente, String motivo);

    /**
     * Transiciona definitivamente la cuenta al estado INACTIVA (dar de baja).
     */
    void darDeBaja(ClienteInstitucional cliente, String motivo);

    /**
     * Restablece el acceso de una cuenta previamente suspendida o dada de baja al estado ACTIVA.
     */
    void reactivar(ClienteInstitucional cliente, String motivo);
}
