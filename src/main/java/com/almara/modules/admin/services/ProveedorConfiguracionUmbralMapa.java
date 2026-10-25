package com.almara.modules.admin.services;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;
import com.almara.modules.admin.patterns.observer.ObservadorCambioUmbralMapa;
import com.almara.modules.admin.patterns.observer.SujetoConfiguracionUmbralMapa;
import com.almara.modules.admin.repositories.RepositorioConfiguracionMapaUmbral;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Patrón Configuration Provider y Sujeto Observable (HU-13).
 * Mantiene en memoria de alta velocidad la configuración activa de umbrales
 * del mapa para accesos O(1) en el motor de agregación y propaga los cambios en caliente.
 */
@Component
public class ProveedorConfiguracionUmbralMapa implements SujetoConfiguracionUmbralMapa {

    private static final Logger log = LoggerFactory.getLogger(ProveedorConfiguracionUmbralMapa.class);

    private final RepositorioConfiguracionMapaUmbral repositorio;
    private final AtomicReference<ConfiguracionMapaUmbral> configuracionActiva = new AtomicReference<>();
    private final List<ObservadorCambioUmbralMapa> observadores = new CopyOnWriteArrayList<>();

    public ProveedorConfiguracionUmbralMapa(RepositorioConfiguracionMapaUmbral repositorio) {
        this.repositorio = repositorio;
    }

    @PostConstruct
    public void inicializar() {
        ConfiguracionMapaUmbral config = repositorio.obtenerConfiguracionGlobal()
                .orElseGet(() -> {
                    log.warn("No se encontró configuración previa en BD. Inicializando valores por defecto para Popayán.");
                    ConfiguracionMapaUmbral defecto = new ConfiguracionMapaUmbral(
                            ConfiguracionMapaUmbral.ID_CONFIGURACION_GLOBAL,
                            5,
                            60,
                            true,
                            "Protección en Comunidad: Para cuidar tu privacidad, las zonas solo se colorean en el mapa cuando al menos 5 vecinos han compartido cómo se sienten en ese lugar. Tu participación es 100% anónima.",
                            LocalDateTime.now(),
                            "SISTEMA"
                    );
                    return repositorio.save(defecto);
                });

        configuracionActiva.set(config);
        log.info("ProveedorConfiguracionUmbralMapa inicializado con umbralK={}, recalculoSg={}, modoEstricto={}",
                config.getUmbralMinimoK(), config.getTiempoRecalculoSegundos(), config.isModoEstricto());
    }

    /**
     * Obtiene la configuración actual en memoria de manera inmediata O(1).
     */
    public ConfiguracionMapaUmbral obtenerConfiguracionActual() {
        ConfiguracionMapaUmbral actual = configuracionActiva.get();
        if (actual == null) {
            inicializar();
            actual = configuracionActiva.get();
        }
        return actual;
    }

    /**
     * Retorna el valor actual de k-anonimato.
     */
    public int obtenerUmbralMinimoK() {
        return obtenerConfiguracionActual().getUmbralMinimoK();
    }

    /**
     * Propaga la nueva configuración en caliente, actualiza la caché volátil y notifica a observadores.
     * Garantiza un tiempo de propagación menor a 500 ms (RNF-08).
     */
    public void propagarNuevaConfiguracion(ConfiguracionMapaUmbral nuevaConfig) {
        long inicioMs = System.currentTimeMillis();
        ConfiguracionMapaUmbral anterior = configuracionActiva.getAndSet(nuevaConfig);

        log.info("Propagando nueva configuración de umbrales del mapa en caliente: previo K={}, nuevo K={}",
                anterior != null ? anterior.getUmbralMinimoK() : "N/A", nuevaConfig.getUmbralMinimoK());

        notificarObservadores(anterior, nuevaConfig);

        long duracionMs = System.currentTimeMillis() - inicioMs;
        log.info("Propagación en caliente de umbrales completada en {} ms (Requisito < 500 ms)", duracionMs);
    }

    @Override
    public void registrarObservador(ObservadorCambioUmbralMapa observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
            log.debug("Observador de umbrales registrado: {}", observador.getClass().getSimpleName());
        }
    }

    @Override
    public void removerObservador(ObservadorCambioUmbralMapa observador) {
        if (observador != null) {
            observadores.remove(observador);
        }
    }

    @Override
    public void notificarObservadores(ConfiguracionMapaUmbral anterior, ConfiguracionMapaUmbral nuevo) {
        for (ObservadorCambioUmbralMapa observador : observadores) {
            try {
                observador.alCambiarUmbral(anterior, nuevo);
            } catch (Exception e) {
                log.error("Error al notificar al observador {}: {}", observador.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
