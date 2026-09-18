package com.almara.modules.aggregation.websocket;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.emotion.models.EventoEmocionRegistrada;
import com.almara.modules.emotion.models.EventoIntensidadRegistrada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Oyente de eventos emocionales para la sincronización del mapa en tiempo real (HU-07)
 * y persistencia continua del estado de zona (HU-05, HU-16).
 * Conecta el bus de eventos interno (Spring Events) con el subsistema de distribución WebSocket (Pub/Sub)
 * y la persistencia en base de datos PostgreSQL.
 */
@Component
public class OyenteEventosEmocionMapa {

    private static final Logger log = LoggerFactory.getLogger(OyenteEventosEmocionMapa.class);

    private final ServicioAgregacionMapa servicioAgregacionMapa;
    private final PublicadorSincronizacionMapa publicadorSincronizacionMapa;

    public OyenteEventosEmocionMapa(ServicioAgregacionMapa servicioAgregacionMapa,
                                   PublicadorSincronizacionMapa publicadorSincronizacionMapa) {
        this.servicioAgregacionMapa = servicioAgregacionMapa;
        this.publicadorSincronizacionMapa = publicadorSincronizacionMapa;
    }

    /**
     * Reacciona inmediatamente ante el registro de una emoción ciudadana.
     * Recalcula el estado de la celda afectada, difunde la actualización por WebSocket
     * y persiste el snapshot en la tabla estado_emocional_zona en PostgreSQL.
     */
    @EventListener
    public void alRegistrarEmocion(EventoEmocionRegistrada evento) {
        if (evento == null || evento.getIdCeldaH3() == null) {
            return;
        }
        actualizarYPersistirCelda(evento.getIdCeldaH3());
    }

    /**
     * Reacciona ante la indicación de intensidad para actualizar el estado agregado si aplica.
     */
    @EventListener
    public void alRegistrarIntensidad(EventoIntensidadRegistrada evento) {
        if (evento == null || evento.getIdCeldaH3() == null) {
            return;
        }
        actualizarYPersistirCelda(evento.getIdCeldaH3());
    }

    private void actualizarYPersistirCelda(String idCeldaH3) {
        log.info("Procesando evento de emoción para sincronización de celda [{}] en tiempo real...", idCeldaH3);

        // Recalcular celda a nivel barrial (zoom 14, umbral 5)
        Optional<CeldaMapaEmocional> celdaOpt = servicioAgregacionMapa.recalcularCelda(idCeldaH3, 14, 5);

        if (celdaOpt.isPresent()) {
            CeldaMapaEmocional celdaActualizada = celdaOpt.get();
            publicadorSincronizacionMapa.publicarActualizacion(
                    PublicadorSincronizacionMapa.CANAL_POPAYAN_DEFAULT,
                    celdaActualizada
            );
            servicioAgregacionMapa.persistirEstadoCelda(celdaActualizada);
            log.info("Sincronización WebSocket y persistencia en PostgreSQL completada para celda [{}]: Emoción=[{}], CumpleUmbral=[{}]",
                    celdaActualizada.getIdCeldaH3(),
                    celdaActualizada.getEmocionPredominante(),
                    celdaActualizada.isCumpleUmbral());
        } else {
            log.warn("No fue posible recalcular celda [{}] para sincronización en tiempo real", idCeldaH3);
        }
    }
}
