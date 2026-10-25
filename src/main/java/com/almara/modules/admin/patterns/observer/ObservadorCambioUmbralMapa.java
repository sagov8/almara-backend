package com.almara.modules.admin.patterns.observer;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;

/**
 * Patrón Observer (HU-13): Interfaz que deben implementar los componentes
 * que requieran reaccionar inmediatamente ante cambios en los umbrales de privacidad
 * o parámetros del mapa emocional.
 */
public interface ObservadorCambioUmbralMapa {

    /**
     * Notificación de variación de umbrales del mapa en caliente.
     *
     * @param anterior Configuración previa
     * @param nuevo    Nueva configuración aplicada
     */
    void alCambiarUmbral(ConfiguracionMapaUmbral anterior, ConfiguracionMapaUmbral nuevo);
}
