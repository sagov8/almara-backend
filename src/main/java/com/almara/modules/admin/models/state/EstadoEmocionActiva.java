package com.almara.modules.admin.models.state;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import org.springframework.http.HttpStatus;

/**
 * Estado Activo de una emoción dentro del catálogo (State Pattern - HU-12).
 */
public class EstadoEmocionActiva implements EstadoEmocion {

    @Override
    public boolean estaActiva() {
        return true;
    }

    @Override
    public void activar(EntidadCatalogoEmocion entidad) {
        // Idempotente: la emoción ya se encuentra activa
        entidad.setActivo(true);
    }

    @Override
    public void desactivar(EntidadCatalogoEmocion entidad, long totalActivasActuales, long minActivasRequeridas) {
        if (totalActivasActuales <= minActivasRequeridas) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "CATALOGO_MINIMO_REQUERIDO",
                    "No es posible desactivar la emoción '" + entidad.getNombreEtiqueta() + "'. El sistema debe mantener al menos "
                            + minActivasRequeridas + " emociones activas para garantizar el funcionamiento del registro ciudadano (RF-05)."
            );
        }
        entidad.setActivo(false);
    }

    @Override
    public String getNombre() {
        return "ACTIVA";
    }
}
