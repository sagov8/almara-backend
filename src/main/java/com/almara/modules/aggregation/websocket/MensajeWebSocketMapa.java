package com.almara.modules.aggregation.websocket;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representa una trama o mensaje de comunicación WebSocket para el mapa colectivo (HU-07).
 * Cumple con RNF Seguridad: No incluye PII, tokens de sesión ni coordenadas GPS individuales;
 * únicamente transmite deltas agregados de celdas espaciales H3.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MensajeWebSocketMapa {

    public static final String TIPO_CONECTADO = "CONECTADO";
    public static final String TIPO_SUSCRITO = "SUSCRITO";
    public static final String TIPO_ACTUALIZACION_CELDA = "ACTUALIZACION_CELDA";
    public static final String TIPO_LATIDO = "LATIDO";
    public static final String TIPO_ERROR = "ERROR";

    private String tipo;
    private String canal;
    private CeldaMapaEmocional celda;
    private String mensaje;
    private long timestamp;

    public MensajeWebSocketMapa() {
        this.timestamp = System.currentTimeMillis();
    }

    public MensajeWebSocketMapa(String tipo, String canal, CeldaMapaEmocional celda, String mensaje) {
        this.tipo = tipo;
        this.canal = canal;
        this.celda = celda;
        this.mensaje = mensaje;
        this.timestamp = System.currentTimeMillis();
    }

    public static MensajeWebSocketMapa conectado(String mensaje) {
        return new MensajeWebSocketMapa(TIPO_CONECTADO, null, null, mensaje);
    }

    public static MensajeWebSocketMapa suscrito(String canal, String mensaje) {
        return new MensajeWebSocketMapa(TIPO_SUSCRITO, canal, null, mensaje);
    }

    public static MensajeWebSocketMapa actualizacionCelda(String canal, CeldaMapaEmocional celda) {
        return new MensajeWebSocketMapa(TIPO_ACTUALIZACION_CELDA, canal, celda, "Actualización colectiva en tiempo real");
    }

    public static MensajeWebSocketMapa latido() {
        return new MensajeWebSocketMapa(TIPO_LATIDO, null, null, "ping");
    }

    public static MensajeWebSocketMapa error(String mensaje) {
        return new MensajeWebSocketMapa(TIPO_ERROR, null, null, mensaje);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public CeldaMapaEmocional getCelda() {
        return celda;
    }

    public void setCelda(CeldaMapaEmocional celda) {
        this.celda = celda;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
