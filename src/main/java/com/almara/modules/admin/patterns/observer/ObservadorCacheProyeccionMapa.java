package com.almara.modules.admin.patterns.observer;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;
import com.almara.modules.admin.services.ProveedorConfiguracionUmbralMapa;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Observador concreto del Patrón Observer (HU-13).
 * Invalida proyecciones espaciales y cachés de celdas en caliente
 * cuando el Administrador actualiza los umbrales mínimos de participación (k-anonimato).
 */
@Component
public class ObservadorCacheProyeccionMapa implements ObservadorCambioUmbralMapa {

    private static final Logger log = LoggerFactory.getLogger(ObservadorCacheProyeccionMapa.class);

    private final ProveedorConfiguracionUmbralMapa proveedorConfiguracion;

    public ObservadorCacheProyeccionMapa(ProveedorConfiguracionUmbralMapa proveedorConfiguracion) {
        this.proveedorConfiguracion = proveedorConfiguracion;
    }

    @PostConstruct
    public void registrar() {
        proveedorConfiguracion.registrarObservador(this);
    }

    @Override
    public void alCambiarUmbral(ConfiguracionMapaUmbral anterior, ConfiguracionMapaUmbral nuevo) {
        log.info("[OBSERVADOR MAPA] Invalidadas cachés de agregación de celdas H3. Nuevo umbral K={}. " +
                        "Las celdas que no alcancen {} reportes pasarán a reserva de privacidad automáticamente.",
                nuevo.getUmbralMinimoK(), nuevo.getUmbralMinimoK());
    }
}
