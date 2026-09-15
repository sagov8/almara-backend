package com.almara.modules.aggregation.websocket;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Publicador central para el patrón Publish-Subscribe en tiempo real (HU-07).
 * Mantiene el registro de sesiones suscritas por canal/tópico y difunde
 * actualizaciones de celdas espaciales de forma desacoplada y concurrente.
 */
@Component
public class PublicadorSincronizacionMapa {

    private static final Logger log = LoggerFactory.getLogger(PublicadorSincronizacionMapa.class);
    public static final String CANAL_POPAYAN_DEFAULT = "/topic/mapa/popayan";

    private final ObjectMapper objectMapper;

    // Mapa de Canales -> Conjunto de Sesiones WebSocket suscritas
    private final Map<String, Set<WebSocketSession>> suscripcionesPorCanal = new ConcurrentHashMap<>();
    // Mapa inverso Sesión -> Canales suscritos para desregistro rápido
    private final Map<String, Set<String>> canalesPorSesion = new ConcurrentHashMap<>();

    public PublicadorSincronizacionMapa(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Suscribe una sesión WebSocket activa a un canal específico.
     */
    public void suscribir(WebSocketSession sesion, String canal) {
        if (sesion == null || !sesion.isOpen()) return;

        String canalNormalizado = normalizarCanal(canal);
        suscripcionesPorCanal
                .computeIfAbsent(canalNormalizado, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(sesion);

        canalesPorSesion
                .computeIfAbsent(sesion.getId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                .add(canalNormalizado);

        log.info("Sesión WebSocket [{}] suscrita al canal [{}] (Total suscriptores en canal: {})",
                sesion.getId(), canalNormalizado, suscripcionesPorCanal.get(canalNormalizado).size());
    }

    /**
     * Desuscribe una sesión de todos los canales tras desconexión.
     */
    public void desuscribir(WebSocketSession sesion) {
        if (sesion == null) return;

        String sesionId = sesion.getId();
        Set<String> canales = canalesPorSesion.remove(sesionId);
        if (canales != null) {
            for (String canal : canales) {
                Set<WebSocketSession> sesiones = suscripcionesPorCanal.get(canal);
                if (sesiones != null) {
                    sesiones.remove(sesion);
                    if (sesiones.isEmpty()) {
                        suscripcionesPorCanal.remove(canal);
                    }
                }
            }
        }
        log.info("Sesión WebSocket [{}] removida de todos los canales", sesionId);
    }

    /**
     * Publica la actualización de una celda espacial a todos los suscriptores del canal.
     * Cumple con RNF Latencia: Difusión directa en memoria sin cuellos de botella (< 2.0 s).
     */
    public void publicarActualizacion(String canal, CeldaMapaEmocional celda) {
        String canalNormalizado = normalizarCanal(canal);
        Set<WebSocketSession> sesiones = suscripcionesPorCanal.get(canalNormalizado);

        if (sesiones == null || sesiones.isEmpty()) {
            log.debug("No hay clientes suscritos en el canal [{}] para la celda [{}]",
                    canalNormalizado, celda != null ? celda.getIdCeldaH3() : "null");
            return;
        }

        MensajeWebSocketMapa mensaje = MensajeWebSocketMapa.actualizacionCelda(canalNormalizado, celda);
        try {
            String json = objectMapper.writeValueAsString(mensaje);
            TextMessage textMessage = new TextMessage(json);

            long inicioDifusion = System.currentTimeMillis();
            int enviadas = 0;

            for (WebSocketSession sesion : sesiones) {
                if (sesion.isOpen()) {
                    try {
                        synchronized (sesion) {
                            sesion.sendMessage(textMessage);
                        }
                        enviadas++;
                    } catch (IOException e) {
                        log.warn("Error enviando actualización a sesión [{}]: {}", sesion.getId(), e.getMessage());
                    }
                }
            }

            long duracionMs = System.currentTimeMillis() - inicioDifusion;
            log.info("Actualización de celda [{}] difundida a {} suscriptores en canal [{}] en {} ms",
                    celda != null ? celda.getIdCeldaH3() : "null", enviadas, canalNormalizado, duracionMs);

        } catch (Exception e) {
            log.error("Error serializando mensaje de actualización para WebSocket: {}", e.getMessage(), e);
        }
    }

    /**
     * Retorna el número de suscriptores actuales en un canal.
     */
    public int contarSuscriptores(String canal) {
        Set<WebSocketSession> sesiones = suscripcionesPorCanal.get(normalizarCanal(canal));
        return sesiones != null ? sesiones.size() : 0;
    }

    private String normalizarCanal(String canal) {
        if (canal == null || canal.isBlank()) {
            return CANAL_POPAYAN_DEFAULT;
        }
        return canal.trim();
    }
}
