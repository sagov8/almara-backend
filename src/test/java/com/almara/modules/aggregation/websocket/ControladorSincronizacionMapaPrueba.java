package com.almara.modules.aggregation.websocket;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.emotion.models.EventoEmocionRegistrada;
import com.almara.modules.emotion.models.TipoEmocion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HU-07: Pruebas de Sincronización en Tiempo Real del Mapa (WebSocket & Pub/Sub)")
public class ControladorSincronizacionMapaPrueba {

    private ObjectMapper objectMapper;
    private PublicadorSincronizacionMapa publicador;
    private ManejadorWebSocketMapa manejador;
    private ServicioAgregacionMapa servicioAgregacionMapa;
    private OyenteEventosEmocionMapa oyenteEventos;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        publicador = new PublicadorSincronizacionMapa(objectMapper);
        manejador = new ManejadorWebSocketMapa(publicador, objectMapper);
        servicioAgregacionMapa = mock(ServicioAgregacionMapa.class);
        oyenteEventos = new OyenteEventosEmocionMapa(servicioAgregacionMapa, publicador);
    }

    @Test
    @DisplayName("Criterio 1: Debe establecer conexión WebSocket y suscribir al canal /topic/mapa/popayan")
    void debeEstablecerConexionYSuscribirAlCanal() throws Exception {
        WebSocketSession sesion = mock(WebSocketSession.class);
        when(sesion.getId()).thenReturn("sesion-prueba-1");
        when(sesion.isOpen()).thenReturn(true);
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost:8080/ws/mapa?canal=/topic/mapa/popayan"));

        manejador.afterConnectionEstablished(sesion);

        assertEquals(1, publicador.contarSuscriptores("/topic/mapa/popayan"));

        ArgumentCaptor<TextMessage> mensajeCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(sesion).sendMessage(mensajeCaptor.capture());

        JsonNode jsonRespuesta = objectMapper.readTree(mensajeCaptor.getValue().getPayload());
        assertEquals("CONECTADO", jsonRespuesta.get("tipo").asText());
        assertEquals("/topic/mapa/popayan", jsonRespuesta.get("canal").asText());
    }

    @Test
    @DisplayName("Criterio 2 y RNF Desempeño: Debe difundir actualización de celda a suscriptores en menos de 2 segundos")
    void debeDifundirActualizacionEnMenosDeDosSegundos() throws Exception {
        WebSocketSession sesion = mock(WebSocketSession.class);
        when(sesion.getId()).thenReturn("sesion-prueba-2");
        when(sesion.isOpen()).thenReturn(true);

        publicador.suscribir(sesion, "/topic/mapa/popayan");

        CeldaMapaEmocional celda = CeldaMapaEmocional.builder()
                .idCeldaH3("8966c6c748fffff")
                .resolucionH3(9)
                .emocionPredominante(TipoEmocion.FELICIDAD)
                .nombreEmocion("Felicidad")
                .codigoHexColor("#10B981")
                .codigoHexFondo("#D1FAE5")
                .totalReportes(8)
                .cumpleUmbral(true)
                .nombreZona("Parque Caldas")
                .build();

        long inicio = System.currentTimeMillis();
        publicador.publicarActualizacion("/topic/mapa/popayan", celda);
        long duracion = System.currentTimeMillis() - inicio;

        // RNF: Latencia inferior a 2000 ms
        assertTrue(duracion < 2000, "La difusión debe tardar menos de 2000 ms, tardó: " + duracion + " ms");

        ArgumentCaptor<TextMessage> mensajeCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(sesion).sendMessage(mensajeCaptor.capture());

        JsonNode payload = objectMapper.readTree(mensajeCaptor.getValue().getPayload());
        assertEquals("ACTUALIZACION_CELDA", payload.get("tipo").asText());
        assertEquals("8966c6c748fffff", payload.get("celda").get("idCeldaH3").asText());
        assertEquals("FELICIDAD", payload.get("celda").get("emocionPredominante").asText());
    }

    @Test
    @DisplayName("Criterio 4: No se transmiten datos ni identificadores individuales durante las actualizaciones (Zero PII)")
    void debeGarantizarQueActualizacionNoExpongaDatosPersonales() throws Exception {
        WebSocketSession sesion = mock(WebSocketSession.class);
        when(sesion.getId()).thenReturn("sesion-prueba-3");
        when(sesion.isOpen()).thenReturn(true);

        publicador.suscribir(sesion, "/topic/mapa/popayan");

        CeldaMapaEmocional celda = CeldaMapaEmocional.builder()
                .idCeldaH3("8966c6c748fffff")
                .resolucionH3(9)
                .emocionPredominante(TipoEmocion.FELICIDAD)
                .nombreEmocion("Felicidad")
                .codigoHexColor("#10B981")
                .codigoHexFondo("#D1FAE5")
                .totalReportes(10)
                .cumpleUmbral(true)
                .nombreZona("Parque Caldas")
                .build();

        publicador.publicarActualizacion("/topic/mapa/popayan", celda);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(sesion).sendMessage(captor.capture());
        String jsonPayload = captor.getValue().getPayload();

        // Verificación estricta de ausencia de PII
        assertFalse(jsonPayload.contains("tokenSesionTemporal"), "No debe exponer token de sesión");
        assertFalse(jsonPayload.contains("usuarioId"), "No debe exponer identificador de usuario");
        assertFalse(jsonPayload.contains("ipCliente"), "No debe exponer dirección IP");
        assertFalse(jsonPayload.contains("gpsPreciso"), "No debe exponer GPS exacto");
        assertTrue(jsonPayload.contains("idCeldaH3"), "Debe contener únicamente el ID espacial de la celda");
    }

    @Test
    @DisplayName("Patrón Pub/Sub: Oyente reacciona a EventoEmocionRegistrada y emite por WebSocket")
    void debeReaccionarAOyenteYPublicarEnWebSocket() throws Exception {
        WebSocketSession sesion = mock(WebSocketSession.class);
        when(sesion.getId()).thenReturn("sesion-oyente");
        when(sesion.isOpen()).thenReturn(true);
        publicador.suscribir(sesion, "/topic/mapa/popayan");

        CeldaMapaEmocional celdaRecalculada = CeldaMapaEmocional.builder()
                .idCeldaH3("8966c6c748fffff")
                .resolucionH3(9)
                .emocionPredominante(TipoEmocion.FELICIDAD)
                .nombreEmocion("Felicidad")
                .codigoHexColor("#10B981")
                .cumpleUmbral(true)
                .nombreZona("Parque Caldas")
                .build();

        when(servicioAgregacionMapa.recalcularCelda("8966c6c748fffff", 14, 5))
                .thenReturn(Optional.of(celdaRecalculada));

        EventoEmocionRegistrada evento = EventoEmocionRegistrada.builder()
                .idEvento(UUID.randomUUID())
                .emocion(TipoEmocion.FELICIDAD)
                .idCeldaH3("8966c6c748fffff")
                .fechaHoraEnvio(Instant.now())
                .build();

        oyenteEventos.alRegistrarEmocion(evento);

        verify(servicioAgregacionMapa).recalcularCelda("8966c6c748fffff", 14, 5);
        verify(sesion).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("Criterio 3: En caso de desconexión, la sesión se desregistra limpiamente")
    void debeDesregistrarSesionAlCerrarConexion() throws Exception {
        WebSocketSession sesion = mock(WebSocketSession.class);
        when(sesion.getId()).thenReturn("sesion-desconectar");
        when(sesion.isOpen()).thenReturn(true);
        when(sesion.getUri()).thenReturn(URI.create("ws://localhost:8080/ws/mapa"));

        manejador.afterConnectionEstablished(sesion);
        assertEquals(1, publicador.contarSuscriptores("/topic/mapa/popayan"));

        manejador.afterConnectionClosed(sesion, CloseStatus.NORMAL);
        assertEquals(0, publicador.contarSuscriptores("/topic/mapa/popayan"));
    }
}
