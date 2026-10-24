package com.almara.modules.admin.models.state;

import com.almara.modules.emotion.models.EntidadCatalogoEmocion;

/**
 * Patrón State (Comportamiento) para el ciclo de vida de una emoción en el catálogo (HU-12).
 * Gestiona las transiciones entre estados (Activa vs Inactiva) y valida las reglas de negocio
 * como el catálogo mínimo requerido para el registro ciudadano (RF-05).
 */
public interface EstadoEmocion {

    /**
     * Indica si la emoción se encuentra disponible para los ciudadanos en la aplicación y mapas.
     */
    boolean estaActiva();

    /**
     * Activa la emoción en el catálogo.
     */
    void activar(EntidadCatalogoEmocion entidad);

    /**
     * Desactiva la emoción mediante borrado lógico, garantizando que el total de emociones activas
     * no descienda por debajo del mínimo reglamentario (RF-05).
     *
     * @param entidad                 Entidad de la emoción a mutar
     * @param totalActivasActuales    Cantidad actual de emociones activas en el catálogo
     * @param minActivasRequeridas    Umbral mínimo indispensable (ej. 3)
     */
    void desactivar(EntidadCatalogoEmocion entidad, long totalActivasActuales, long minActivasRequeridas);

    /**
     * Nombre descriptivo del estado actual.
     */
    String getNombre();
}
