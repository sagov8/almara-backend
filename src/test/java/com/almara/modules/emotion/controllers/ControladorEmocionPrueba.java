package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.emotion.services.ServicioControlFrecuencia;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorEmocionPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapeadorObjetos;

    @Autowired
    private ServicioControlFrecuencia servicioControlFrecuencia;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @BeforeEach
    void prepararPrueba() {
        servicioControlFrecuencia.limpiarBloqueos();
        repositorioReporteEmocion.limpiar();
    }

    @Test
    @DisplayName("HU-01 y HU-04: Registro exitoso con coordenadas GPS temporales convertidas a celda H3")
    void registrarEmocion_ConGps_Exitoso() throws Exception {
        String tokenPrueba = "token-anonimo-" + UUID.randomUUID();
        // Coordenadas en Chapinero, Bogotá
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.FELICIDAD)
                .tokenSesionTemporal(tokenPrueba)
                .latitud(4.6486)
                .longitud(-74.0645)
                .build();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvento").isNotEmpty())
                .andExpect(jsonPath("$.mensaje", containsString("éxito")))
                .andExpect(jsonPath("$.emocionRegistrada").value("FELICIDAD"))
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty())
                .andExpect(jsonPath("$.idCeldaH3", hasLength(15))) // Longitud estándar celda H3 hex
                .andExpect(jsonPath("$.fechaHoraEnvio").isNotEmpty())
                .andExpect(jsonPath("$.segundosBloqueo").value(900))
                // RNF Seguridad: No deben exponerse coordenadas GPS en la respuesta
                .andExpect(jsonPath("$.latitud").doesNotExist())
                .andExpect(jsonPath("$.longitud").doesNotExist());
    }

    @Test
    @DisplayName("HU-04: Registro exitoso mediante selección manual de zona del catálogo")
    void registrarEmocion_ConZonaManual_Exitoso() throws Exception {
        String tokenPrueba = "token-anonimo-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.NEUTRALIDAD)
                .tokenSesionTemporal(tokenPrueba)
                .zonaManualId("ZONA-CHAPINERO")
                .build();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvento").isNotEmpty())
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty())
                .andExpect(jsonPath("$.nombreZona").value("Chapinero Central"));
    }

    @Test
    @DisplayName("HU-04 Criterio 4: Rechazo con HTTP 400 cuando no se proveen ni GPS ni zona manual")
    void registrarEmocion_SinUbicacion_Rechazado() throws Exception {
        String tokenPrueba = "token-anonimo-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.PREOCUPACION)
                .tokenSesionTemporal(tokenPrueba)
                .build();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("UBICACION_REQUERIDA"))
                .andExpect(jsonPath("$.mensaje", containsString("autorización de ubicación GPS o selección manual")));
    }

    @Test
    @DisplayName("HU-04 RNF Seguridad: Comprobación de que las coordenadas no persisten en base de datos")
    void registrarEmocion_VerificarPersistenciaSinCoordenadas() throws Exception {
        String tokenPrueba = "token-anonimo-seguridad-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.ANSIEDAD)
                .tokenSesionTemporal(tokenPrueba)
                .latitud(4.6974)
                .longitud(-74.0298)
                .build();

        MvcResult resultado = mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn();

        String idCeldaH3Respuesta = mapeadorObjetos.readTree(resultado.getResponse().getContentAsString())
                .get("idCeldaH3").asText();

        List<ReporteEmocion> reportesEnCelda = repositorioReporteEmocion.buscarPorCeldaH3(idCeldaH3Respuesta);
        assertEquals(1, reportesEnCelda.size());
        ReporteEmocion guardado = reportesEnCelda.get(0);
        assertNotNull(guardado.getIdCeldaH3());
        assertEquals(TipoEmocion.ANSIEDAD, guardado.getEmocion());
        // ReporteEmocion no posee atributos de latitud ni longitud
    }

    @Test
    @DisplayName("HU-01: Rechazo con HTTP 400 ante emoción no catalogada")
    void registrarEmocion_EmocionInvalida() throws Exception {
        String jsonInvalido = """
                {
                    "emocion": "DESCONOCIDA",
                    "tokenSesionTemporal": "token-anonimo-123456789",
                    "zonaManualId": "ZONA-CHAPINERO"
                }
                """;

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigoEstado").value(400));
    }

    @Test
    @DisplayName("HU-01: Rechazo con HTTP 400 cuando no se envía token de sesión temporal")
    void registrarEmocion_TokenFaltante() throws Exception {
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.ANSIEDAD)
                .tokenSesionTemporal("")
                .zonaManualId("ZONA-CHAPINERO")
                .build();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION_FALLIDA"));
    }

    @Test
    @DisplayName("HU-01: Control de frecuencia / unicidad (HTTP 429) por envíos repetidos dentro del lapso")
    void registrarEmocion_BloqueoPorFrecuencia() throws Exception {
        String tokenPrueba = "token-anonimo-bloqueo-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.ENFADO)
                .tokenSesionTemporal(tokenPrueba)
                .zonaManualId("ZONA-CHAPINERO")
                .build();

        // Primer envío: Exitoso
        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated());

        // Segundo envío inmediato con el mismo token: Retorna HTTP 429
        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("SOLICITUDES_EXCESIVAS"))
                .andExpect(jsonPath("$.detallesAdicionales.segundosRestantes", greaterThan(0)));
    }

    @Test
    @DisplayName("HU-01 y HU-04: Tiempo de respuesta inferior a 1000 ms (RNF Rendimiento)")
    void registrarEmocion_DesempenoInferiorA1000ms() throws Exception {
        String tokenPrueba = "token-anonimo-rendimiento-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.NEUTRALIDAD)
                .tokenSesionTemporal(tokenPrueba)
                .latitud(4.6534)
                .longitud(-74.1086)
                .build();

        long inicio = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated());

        long duracion = System.currentTimeMillis() - inicio;
        assertTrue(duracion < 1000, "El tiempo de respuesta debe ser inferior a 1000 ms, fue: " + duracion + " ms");
    }

    @Test
    @DisplayName("HU-01: Consulta del catálogo predeterminado de emociones")
    void obtenerCatalogo_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/emociones/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].etiquetaVisible", hasItems("Felicidad", "Neutralidad", "Preocupación", "Enfado", "Ansiedad")));
    }
}
