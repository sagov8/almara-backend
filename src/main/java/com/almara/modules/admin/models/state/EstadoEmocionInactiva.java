package com.almara.modules.admin.models.state;

import com.almara.modules.emotion.models.EntidadCatalogoEmocion;

/**
 * Estado Inactivo de una emoción dentro del catálogo (State Pattern - HU-12).
 */
public class EstadoEmocionInactiva implements EstadoEmocion {

    @Override
    public boolean estaActiva() {
        return false;
    }

    @Override
    public void activar(EntidadCatalogoEmocion entidad) {
        entidad.setActivo(true);
    }

    @Override
    public void desactivar(EntidadCatalogoEmocion entidad, long totalActivasActuales, long minActivasRequeridas) {
        // Idempotente: la emoción ya se encuentra inactiva
        entidad.setActivo(false);
    }

    @Override
    public String getNombre() {
        return "INACTIVA";
    }
}
