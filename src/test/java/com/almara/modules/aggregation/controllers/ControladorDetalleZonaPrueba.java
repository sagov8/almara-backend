package com.almara.modules.aggregation.controllers;

import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.aggregation.services.ServicioDetalleZona;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración automatizadas para la consulta de detalle de zona (HU-06).
 * Valida la completitud matemática (porcentajes = 100%), participantes aproximados por privacidad,
 * tiempo de respuesta < 300 ms, tendencia, comentarios e inclusión de períodos temporales.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorDetalleZonaPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @Autowired
    private RepositorioZona repositorioZona;

    @Autowired
    private ServicioAgregacionMapa servicioAgregacionMapa;

    @Autowired
    private ServicioDetalleZona servicioDetalleZona;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repositorioReporteEmocion.limpiar();
        servicioDetalleZona.limpiarCache();
        servicioAgregacionMapa.sembrarReportesIniciales();
    }

    @Test
    @DisplayName("HU-06 RNF Desempeño: Consulta de detalle de zona responde en menos de 300 ms")
    void debeConsultarDetalleZonaConExitoYResponderEnMenosDeTrescientosMs() throws Exception {
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        long t0 = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/mapa/zona/" + caldas.getIdCeldaH3() + "/detalle")
                        .param("periodo", "ULTIMAS_2_HORAS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCeldaH3").value(caldas.getIdCeldaH3()))
                .andExpect(jsonPath("$.nombreZona").value("Parque Caldas"))
                .andExpect(jsonPath("$.cumpleUmbral").value(true))
                .andExpect(jsonPath("$.tiempoCalculoMs", lessThan(300)));

        long tiempoTranscurrido = System.currentTimeMillis() - t0;
        assertTrue(tiempoTranscurrido < 300, "El tiempo de respuesta debe ser inferior a 300 ms");
    }

    @Test
    @DisplayName("HU-06 Criterio 1 y RNF Completitud: Los porcentajes de la distribución suman exactamente 100%")
    void debeGarantizarQueLaSumaDePorcentajesEmocionalesSeaExactamenteCienPorCiento() throws Exception {
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        MvcResult resultado = mockMvc.perform(get("/api/v1/mapa/zona/" + caldas.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(resultado.getResponse().getContentAsString());
        JsonNode porcentajesNode = root.get("distribucionPorcentajes");
        assertNotNull(porcentajesNode, "El mapa de distribución porcentual no debe ser nulo");

        int suma = 0;
        Iterator<Map.Entry<String, JsonNode>> fields = porcentajesNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            suma += field.getValue().asInt();
        }

        assertEquals(100, suma, "La suma de los porcentajes emocionales debe ser exactamente 100%");
    }

    @Test
    @DisplayName("HU-06 Criterio 3 y RNF Privacidad: Oculta cifra exacta y presenta participantes aproximados")
    void debeOcultarCifraExactaYPresentarParticipantesAproximados() throws Exception {
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/zona/" + caldas.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                // No debe exponer campo de total exacto de personas en el DTO
                .andExpect(jsonPath("$.totalEventosExactos").doesNotExist())
                .andExpect(jsonPath("$.totalReportes").doesNotExist())
                // Debe presentar participantes aproximados
                .andExpect(jsonPath("$.participantesAproximados", matchesPattern("^(Más de|Menos de) [0-9]+ vecinos$")));
    }

    @Test
    @DisplayName("HU-06 Criterio 2: Especifica claramente el período temporal utilizado para la métrica")
    void debeEspecificarPeriodoTemporalUtilizadoEnLaMetrica() throws Exception {
        Zona morro = repositorioZona.buscarPorZonaManualId("ZONA-MORRO-TULCAN").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/zona/" + morro.getIdCeldaH3() + "/detalle")
                        .param("periodo", "ULTIMAS_24_HORAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodoTemporal").value("Últimas 24 horas"));
    }

    @Test
    @DisplayName("HU-06 Criterio 1: Despliega la emoción predominante y la tendencia actual de la comunidad")
    void debeDesplegarEmocionPredominanteYTendenciaActual() throws Exception {
        Zona morro = repositorioZona.buscarPorZonaManualId("ZONA-MORRO-TULCAN").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/zona/" + morro.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emocionPredominante").value("FELICIDAD"))
                .andExpect(jsonPath("$.nombreEmocion").value("Felicidad"))
                .andExpect(jsonPath("$.tendencia", not(emptyString())));
    }

    @Test
    @DisplayName("HU-06 Privacidad y Umbral: Zonas con menos de 5 reportes se marcan como en reserva ciudadana")
    void debeProtegerZonaEnReservaCuandoNoAlcanzaUmbralMinimo() throws Exception {
        // Panteón de los Próceres solo tiene 2 reportes (< 5)
        Zona panteon = repositorioZona.buscarPorZonaManualId("ZONA-PANTEON-PROCERES").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/zona/" + panteon.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cumpleUmbral").value(false))
                .andExpect(jsonPath("$.nombreEmocion").value("En reserva ciudadana"))
                .andExpect(jsonPath("$.icono").value("🔒"))
                .andExpect(jsonPath("$.emocionPredominante").value(nullValue()))
                .andExpect(jsonPath("$.participantesAproximados").value("Menos de 5 vecinos"))
                .andExpect(jsonPath("$.descripcionProteccion", containsString("Se necesitan al menos 5 opiniones")));
    }

    @Test
    @DisplayName("HU-06 Integración HU-03: Incluye comentarios moderados recientes de la zona")
    void debeIncluirComentariosAnonimosModeradosAsociadosALaZona() throws Exception {
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        // Agregar reporte con comentario anónimo y moderado
        ReporteEmocion reporteConComentario = ReporteEmocion.builder()
                .idEvento(UUID.randomUUID())
                .idCeldaH3(caldas.getIdCeldaH3())
                .emocion(TipoEmocion.FELICIDAD)
                .intensidad(4.0f)
                .comentario("Tarde tranquila y agradable en el parque")
                .fechaHora(Instant.now())
                .tokenSesionTemporal(UUID.randomUUID().toString())
                .build();
        repositorioReporteEmocion.guardar(reporteConComentario);
        servicioDetalleZona.limpiarCache();

        mockMvc.perform(get("/api/v1/mapa/zona/" + caldas.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentariosRecientes", hasItem("Tarde tranquila y agradable en el parque")));
    }

    @Test
    @DisplayName("HU-06 Lenguaje Ciudadano: Explicación amigable de Protección en Comunidad sin tecnicismos")
    void debeRetornarMensajeProteccionComunidadAmigableSinJerga() throws Exception {
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/zona/" + caldas.getIdCeldaH3() + "/detalle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcionProteccion", containsString("Protección en Comunidad")))
                .andExpect(jsonPath("$.descripcionProteccion", not(containsString("k-anonimato"))))
                .andExpect(jsonPath("$.descripcionProteccion", not(containsString("ataque de inferencia"))));
    }
}
