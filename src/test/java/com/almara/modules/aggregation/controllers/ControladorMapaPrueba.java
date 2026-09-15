package com.almara.modules.aggregation.controllers;

import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para la consulta del mapa interactivo (HU-05).
 * Valida el RNF de desempeño (< 2.0s), la aplicación de la estrategia espacial según zoom,
 * el principio de Protección en Comunidad (ocultar zonas < umbral) y el lenguaje amigable.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorMapaPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @Autowired
    private RepositorioZona repositorioZona;

    @Autowired
    private ServicioAgregacionMapa servicioAgregacionMapa;

    @BeforeEach
    void setUp() {
        repositorioReporteEmocion.limpiar();
        servicioAgregacionMapa.sembrarReportesIniciales();
    }

    @Test
    @DisplayName("HU-05 Criterio 1 y RNF Desempeño: Consulta de mapa Popayán responde exitosamente en < 2000 ms")
    void debeConsultarMapaPopayanConExitoYMenosDeDosSegundos() throws Exception {
        long t0 = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/mapa/popayan")
                        .param("zoom", "14")
                        .param("umbralK", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.celdasVisibles", not(empty())))
                .andExpect(jsonPath("$.totalCeldasVisibles", greaterThan(0)))
                .andExpect(jsonPath("$.resolucionH3Utilizada").value(9))
                .andExpect(jsonPath("$.umbralMinimoAplicado").value(5))
                .andExpect(jsonPath("$.tiempoCalculoMs", lessThan(2000)));

        long transcurrido = System.currentTimeMillis() - t0;
        assertTrue(transcurrido < 2000, "El tiempo de respuesta del endpoint debe ser inferior a 2.0 segundos");
    }

    @Test
    @DisplayName("HU-05 Criterio 2 y 3: Zonas sin datos suficientes (k < 5) permanecen ocultas en celdasVisibles")
    void debeOcultarZonasQueNoCumplenUmbralMinimoK() throws Exception {
        // En sembrarReportesIniciales(), ZONA-PANTEON-PROCERES solo tiene 2 reportes (< 5)
        mockMvc.perform(get("/api/v1/mapa/popayan")
                        .param("zoom", "14")
                        .param("umbralK", "5"))
                .andExpect(status().isOk())
                // Verificar que ninguna celda visible tenga menos de 5 reportes
                .andExpect(jsonPath("$.celdasVisibles[*].totalReportes", everyItem(greaterThanOrEqualTo(5))))
                // Panteón de los próceres no debe figurar entre las celdas visibles
                .andExpect(jsonPath("$.celdasVisibles[?(@.nombreZona == 'Panteón de los Próceres')]", empty()));
    }

    @Test
    @DisplayName("HU-05 Criterio 2: Muestra la zona cuando alcanza el umbral mínimo k")
    void debeMostrarZonaCuandoAlcanzaUmbralK() throws Exception {
        // Obtenemos una zona y agregamos 5 reportes
        Zona caldas = repositorioZona.buscarPorZonaManualId("ZONA-PARQUE-CALDAS").orElseThrow();

        mockMvc.perform(get("/api/v1/mapa/celdas")
                        .param("zoom", "14")
                        .param("umbralK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.celdasVisibles[?(@.idCeldaH3 == '" + caldas.getIdCeldaH3() + "')]", not(empty())))
                .andExpect(jsonPath("$.celdasVisibles[?(@.idCeldaH3 == '" + caldas.getIdCeldaH3() + "')].cumpleUmbral").value(hasItem(true)))
                .andExpect(jsonPath("$.celdasVisibles[?(@.idCeldaH3 == '" + caldas.getIdCeldaH3() + "')].nombreEmocion").value(hasItem("Felicidad")))
                .andExpect(jsonPath("$.celdasVisibles[?(@.idCeldaH3 == '" + caldas.getIdCeldaH3() + "')].codigoHexColor").value(hasItem("#10B981")));
    }

    @Test
    @DisplayName("HU-05 Patrón Strategy: Selecciona EstrategiaZoomDetalle (Res 9) para zoom >= 14")
    void debeAplicarEstrategiaZoomDetalleParaZoomAlto() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/celdas")
                        .param("zoom", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolucionH3Utilizada").value(9));
    }

    @Test
    @DisplayName("HU-05 Patrón Strategy: Selecciona EstrategiaZoomMedio (Res 8) para zoom entre 11 y 13")
    void debeAplicarEstrategiaZoomMedioParaZoomIntermedio() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/celdas")
                        .param("zoom", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolucionH3Utilizada").value(8));
    }

    @Test
    @DisplayName("HU-05 Patrón Strategy: Selecciona EstrategiaZoomMacro (Res 7) para zoom < 11")
    void debeAplicarEstrategiaZoomMacroParaZoomBajo() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/celdas")
                        .param("zoom", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolucionH3Utilizada").value(7));
    }

    @Test
    @DisplayName("HU-05 RNF Usabilidad y Seguridad: Mensaje accesible de Protección en Comunidad sin jerga técnica")
    void debeRetornarMensajeProteccionComunidadAmigableSinJerga() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/popayan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensajeProteccionComunidad", containsString("Protección en Comunidad")))
                .andExpect(jsonPath("$.mensajeProteccionComunidad", containsString("al menos 5 vecinos")))
                .andExpect(jsonPath("$.mensajeProteccionComunidad", not(containsString("k-anonimato"))))
                .andExpect(jsonPath("$.mensajeProteccionComunidad", not(containsString("ataque de inferencia"))));
    }

    @Test
    @DisplayName("HU-05 RNF Seguridad: No expone identificadores personales ni coordenadas GPS exactas")
    void debeNoExponerDatosDeUsuariosIndividualesNiCoordenadasGpsExactas() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/popayan"))
                .andExpect(status().isOk())
                // Validar que el payload no contenga campos de usuario o tokens individuales
                .andExpect(jsonPath("$.celdasVisibles[*].idEvento").doesNotExist())
                .andExpect(jsonPath("$.celdasVisibles[*].tokenSesionTemporal").doesNotExist())
                .andExpect(jsonPath("$.celdasVisibles[*].idUsuario").doesNotExist())
                .andExpect(jsonPath("$.celdasVisibles[*].comentarioContexto").doesNotExist())
                // Debe contener centroide de celda y vértices del polígono
                .andExpect(jsonPath("$.celdasVisibles[0].latitudCentroide").isNumber())
                .andExpect(jsonPath("$.celdasVisibles[0].longitudCentroide").isNumber())
                .andExpect(jsonPath("$.celdasVisibles[0].verticesHexagono", not(empty())));
    }

    @Test
    @DisplayName("HU-05 Filtrado espacial: Bounding box fuera de Popayán retorna 0 celdas")
    void debeFiltrarPorBoundingBoxGeografico() throws Exception {
        // Coordenadas en Tokio, Japón
        mockMvc.perform(get("/api/v1/mapa/celdas")
                        .param("minLat", "35.60")
                        .param("maxLat", "35.70")
                        .param("minLon", "139.60")
                        .param("maxLon", "139.80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCeldasVisibles").value(0))
                .andExpect(jsonPath("$.celdasVisibles", empty()));
    }

    @Test
    @DisplayName("HU-05 Endpoint /todas-celdas: Permite auditar celdas en reserva marcadas con candado y sin emoción expuesta")
    void debeRetornarCeldasProtegidasEnEndpointTodasCeldasConIconoCandado() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/todas-celdas")
                        .param("zoom", "14")
                        .param("umbralK", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.cumpleUmbral == false)]", not(empty())))
                .andExpect(jsonPath("$[?(@.cumpleUmbral == false)].icono").value(hasItem("🔒")))
                .andExpect(jsonPath("$[?(@.cumpleUmbral == false)].nombreEmocion").value(hasItem("Protegida")))
                .andExpect(jsonPath("$[?(@.cumpleUmbral == false)].emocionPredominante").value(hasItem(nullValue())));
    }
}
