package com.almara.config;

import com.almara.modules.aggregation.websocket.ManejadorWebSocketMapa;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Configuración de WebSocket para la sincronización en tiempo real del mapa interactivo (HU-07).
 * Registra el endpoint nativo /ws/mapa permitiendo orígenes cruzados (CORS) para clientes web y móviles.
 */
@Configuration
@EnableWebSocket
public class ConfiguracionWebSocket implements WebSocketConfigurer {

    private final ManejadorWebSocketMapa manejadorWebSocketMapa;

    public ConfiguracionWebSocket(ManejadorWebSocketMapa manejadorWebSocketMapa) {
        this.manejadorWebSocketMapa = manejadorWebSocketMapa;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(manejadorWebSocketMapa, "/ws/mapa")
                .setAllowedOrigins("*");
    }
}
