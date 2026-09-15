package com.almara.modules.aggregation.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;

/**
 * Manejador de eventos y ciclo de vida de conexiones WebSocket para el mapa colectivo (HU-07).
 * Procesa handshake, suscripción a canales, deserialización de comandos y desconexiones limpias.
 */
@Component
public class ManejadorWebSocketMapa extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorWebSocketMapa.class);

    private final PublicadorSincronizacionMapa publicador;
    private final ObjectMapper objectMapper;

    public ManejadorWebSocketMapa(PublicadorSincronizacionMapa publicador, ObjectMapper objectMapper) {
        this.publicador = publicador;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String canalInicial = extraerCanalDeUri(session.getUri());
        publicador.suscribir(session, canalInicial);

        MensajeWebSocketMapa bienvenida = MensajeWebSocketMapa.conectado(
                "Conexión en tiempo real con Almara establecida. Canal activo: " + canalInicial);
        bienvenida.setCanal(canalInicial);

        synchronized (session) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(bienvenida)));
        }

        log.info("Cliente WebSocket conectado exitosamente: [ID: {}, Canal: {}]",
                session.getId(), canalInicial);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Mensaje recibido de sesión [{}]: {}", session.getId(), payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            String tipo = root.has("tipo") ? root.get("tipo").asText() : "";

            if ("SUSCRIBIR".equalsIgnoreCase(tipo)) {
                String nuevoCanal = root.has("canal") ? root.get("canal").asText() : PublicadorSincronizacionMapa.CANAL_POPAYAN_DEFAULT;
                publicador.suscribir(session, nuevoCanal);

                MensajeWebSocketMapa respuesta = MensajeWebSocketMapa.suscrito(nuevoCanal, "Suscripción confirmada al canal " + nuevoCanal);
                synchronized (session) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(respuesta)));
                }
            } else if ("LATIDO".equalsIgnoreCase(tipo) || "PING".equalsIgnoreCase(tipo)) {
                MensajeWebSocketMapa pong = MensajeWebSocketMapa.latido();
                pong.setMensaje("PONG");
                synchronized (session) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
                }
            }
        } catch (Exception e) {
            log.warn("Mensaje WebSocket no procesable de sesión [{}]: {}", session.getId(), e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        publicador.desuscribir(session);
        log.info("Cliente WebSocket desconectado: [ID: {}, Razón: {}]", session.getId(), status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.warn("Error de transporte en sesión WebSocket [{}]: {}", session.getId(), exception.getMessage());
        publicador.desuscribir(session);
    }

    private String extraerCanalDeUri(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return PublicadorSincronizacionMapa.CANAL_POPAYAN_DEFAULT;
        }

        String query = uri.getQuery();
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length == 2 && "canal".equalsIgnoreCase(par[0])) {
                return par[1];
            }
        }

        return PublicadorSincronizacionMapa.CANAL_POPAYAN_DEFAULT;
    }
}
