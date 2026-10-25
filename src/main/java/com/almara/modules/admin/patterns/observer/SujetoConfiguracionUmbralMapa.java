package com.almara.modules.admin.patterns.observer;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;

/**
 * Patrón Observer (HU-13): Sujeto observable que administra y despacha notificaciones
 * a los observadores registrados cuando se ajustan los umbrales de privacidad.
 */
public interface SujetoConfiguracionUmbralMapa {

    void registrarObservador(ObservadorCambioUmbralMapa observador);

    void removerObservador(ObservadorCambioUmbralMapa observador);

    void notificarObservadores(ConfiguracionMapaUmbral anterior, ConfiguracionMapaUmbral nuevo);
}
