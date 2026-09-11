package com.almara.modules.emotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
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

    @BeforeEach
    void prepararPrueba() {
        servicioControlFrecuencia.limpiarBloqueos();
    }

    @Test
    @DisplayName("HU-01: Registro exitoso de emoción con confirmación y marca temporal")
    void registrarEmocion_Exitoso() throws Exception {
        String tokenPrueba = "token-anonimo-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.FELICIDAD)
                .tokenSesionTemporal(tokenPrueba)
                .build();

        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEvento").isNotEmpty())
                .andExpect(jsonPath("$.mensaje", containsString("éxito")))
                .andExpect(jsonPath("$.emocionRegistrada").value("FELICIDAD"))
                .andExpect(jsonPath("$.fechaHoraEnvio").isNotEmpty())
                .andExpect(jsonPath("$.segundosBloqueo").value(900));
    }

    @Test
    @DisplayName("HU-01: Rechazo con HTTP 400 ante emoción no catalogada")
    void registrarEmocion_EmocionInvalida() throws Exception {
        String jsonInvalido = """
                {
                    "emocion": "DESCONOCIDA",
                    "tokenSesionTemporal": "token-anonimo-123456789"
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
                .build();

        // Primer envío: Debe ser exitoso
        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isCreated());

        // Segundo envío inmediato con el mismo token: Debe retornar HTTP 429
        mockMvc.perform(post("/api/v1/emociones/seleccionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("SOLICITUDES_EXCESIVAS"))
                .andExpect(jsonPath("$.detallesAdicionales.segundosRestantes", greaterThan(0)));
    }

    @Test
    @DisplayName("HU-01: Tiempo de respuesta inferior a 1000 ms (RNF Rendimiento)")
    void registrarEmocion_DesempenoInferiorA1000ms() throws Exception {
        String tokenPrueba = "token-anonimo-rendimiento-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.NEUTRALIDAD)
                .tokenSesionTemporal(tokenPrueba)
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
