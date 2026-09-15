package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.models.RegistroIntensidadSolicitud;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.emotion.services.ServicioControlFrecuencia;
import com.almara.modules.emotion.services.ServicioRegistroEmocion;
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorIntensidadPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapeadorObjetos;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @Autowired
    private ServicioRegistroEmocion servicioRegistroEmocion;

    @Autowired
    private ServicioControlFrecuencia servicioControlFrecuencia;

    @BeforeEach
    void prepararPrueba() {
        repositorioReporteEmocion.limpiar();
        servicioControlFrecuencia.limpiarBloqueos();
    }

    private UUID registrarEmocionPrevia(TipoEmocion emocion) {
        String tokenPrueba = "token-anonimo-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(emocion)
                .tokenSesionTemporal(tokenPrueba)
                .zonaManualId("ZONA-PARQUE-CALDAS")
                .build();
        return servicioRegistroEmocion.registrarEmocion(solicitud).getIdEvento();
    }

    @Test
    @DisplayName("HU-02: Registro exitoso de intensidad (Nivel 3 - Moderado) asociado a evento previo")
    void registrarIntensidad_Exitoso() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.FELICIDAD);

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(3)
                .build();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvento").value(idEvento.toString()))
                .andExpect(jsonPath("$.nivelIntensidad").value(3))
                .andExpect(jsonPath("$.etiquetaIntensidad").value("Moderado"))
                .andExpect(jsonPath("$.emocion").value("FELICIDAD"))
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty())
                .andExpect(jsonPath("$.mensaje", containsString("éxito")))
                .andExpect(jsonPath("$.fechaHoraEnvio").isNotEmpty());

        // Verificar persistencia en base de datos/repositorio
        ReporteEmocion reporteActualizado = repositorioReporteEmocion.buscarPorId(idEvento).orElseThrow();
        assertNotNull(reporteActualizado.getIntensidad());
        assertEquals(3.0f, reporteActualizado.getIntensidad());
    }

    @Test
    @DisplayName("HU-02: Registro exitoso mediante ruta semántica PATCH /{idEvento}/intensidad (Nivel 5 - Intenso)")
    void registrarIntensidad_PorRutaPatch_Exitoso() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ANSIEDAD);

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .nivelIntensidad(5)
                .build();

        mockMvc.perform(patch("/api/v1/emociones/" + idEvento + "/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvento").value(idEvento.toString()))
                .andExpect(jsonPath("$.nivelIntensidad").value(5))
                .andExpect(jsonPath("$.etiquetaIntensidad").value("Intenso"));
    }

    @Test
    @DisplayName("HU-02 Criterio de Inmutabilidad: Rechazo con HTTP 409 Conflict si la intensidad ya fue registrada")
    void registrarIntensidad_Inmutabilidad_Rechazo409() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ENFADO);

        RegistroIntensidadSolicitud primeraSolicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(2)
                .build();

        // Primer registro exitoso
        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(primeraSolicitud)))
                .andExpect(status().isOk());

        // Intento de reescritura o modificación (violación de inmutabilidad)
        RegistroIntensidadSolicitud segundaSolicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(4)
                .build();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(segundaSolicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INTENSIDAD_YA_REGISTRADA"))
                .andExpect(jsonPath("$.mensaje", containsString("inmutable")));
    }

    @Test
    @DisplayName("HU-02 Criterio Escala Cerrada: Rechazo con HTTP 400 ante intensidad inferior a 1")
    void registrarIntensidad_ValorInferiorA1_Rechazado() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.PREOCUPACION);

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(0)
                .build();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION_FALLIDA"))
                .andExpect(jsonPath("$.detallesAdicionales.nivelIntensidad").isNotEmpty());
    }

    @Test
    @DisplayName("HU-02 Criterio Escala Cerrada: Rechazo con HTTP 400 ante intensidad superior a 5")
    void registrarIntensidad_ValorSuperiorA5_Rechazado() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.PREOCUPACION);

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(6)
                .build();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION_FALLIDA"));
    }

    @Test
    @DisplayName("HU-02: Rechazo con HTTP 404 si el idEvento no existe en el sistema")
    void registrarIntensidad_EventoInexistente_Rechazo404() throws Exception {
        UUID idEventoInexistente = UUID.randomUUID();

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEventoInexistente)
                .nivelIntensidad(1)
                .build();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("REPORTE_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensaje", containsString(idEventoInexistente.toString())));
    }

    @Test
    @DisplayName("HU-02 RNF Desempeño: Tiempo de respuesta garantizado inferior a 300 ms")
    void registrarIntensidad_DesempenoInferiorA300ms() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.NEUTRALIDAD);

        RegistroIntensidadSolicitud solicitud = RegistroIntensidadSolicitud.builder()
                .idEvento(idEvento)
                .nivelIntensidad(3)
                .build();

        long inicio = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/emociones/intensidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk());

        long duracion = System.currentTimeMillis() - inicio;
        assertTrue(duracion < 300, "El tiempo de procesamiento debe ser inferior a 300 ms, fue: " + duracion + " ms");
    }
}
