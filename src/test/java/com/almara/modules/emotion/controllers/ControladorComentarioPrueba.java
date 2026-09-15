package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.*;
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

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorComentarioPrueba {

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
    @DisplayName("HU-03: Registro exitoso de comentario complementario válido (Criterio 1 y 2)")
    void registrarComentario_Exitoso() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.PREOCUPACION);

        String textoComentario = "Hay mucho ruido por las obras cercanas que me dificulta concentrarme.";
        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario(textoComentario)
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvento").value(idEvento.toString()))
                .andExpect(jsonPath("$.comentario").value(textoComentario))
                .andExpect(jsonPath("$.commentContext").value(textoComentario))
                .andExpect(jsonPath("$.mensaje", containsString("éxito")))
                .andExpect(jsonPath("$.emocion").value("PREOCUPACION"))
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty());

        // Verificar persistencia en base de datos
        ReporteEmocion reporteGuardado = repositorioReporteEmocion.buscarPorId(idEvento).orElseThrow();
        assertEquals(textoComentario, reporteGuardado.getComentario());
    }

    @Test
    @DisplayName("HU-03: Registro exitoso mediante ruta semántica PATCH /{idEvento}/comentario")
    void registrarComentario_PorRutaPatch_Exitoso() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.FELICIDAD);

        String textoComentario = "Hermoso atardecer y clima agradable en el parque.";
        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .comentario(textoComentario)
                .build();

        mockMvc.perform(patch("/api/v1/emociones/" + idEvento + "/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvento").value(idEvento.toString()))
                .andExpect(jsonPath("$.comentario").value(textoComentario))
                .andExpect(jsonPath("$.emocion").value("FELICIDAD"));
    }

    @Test
    @DisplayName("HU-03 Criterio 2: Rechazo con HTTP 400 si el comentario incluye números")
    void registrarComentario_ConNumeros_Rechazado() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ANSIEDAD);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario("Había más de 50 personas esperando en la fila.")
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CARACTERES_INVALIDOS"))
                .andExpect(jsonPath("$.mensaje", containsString("números o dígitos")));
    }

    @Test
    @DisplayName("HU-03 Criterio 1: Rechazo con HTTP 400 si supera los 200 caracteres")
    void registrarComentario_LongitudExcedida_Rechazado() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ENFADO);

        // Generar texto de 205 caracteres sin dígitos
        String comentarioLargo = "Este es un texto excesivamente extenso que supera los doscientos caracteres reglamentarios " +
                "para probar que la validación estricta del backend rechaza de forma adecuada comentarios que no cumplen " +
                "con el límite fijado.";

        assertTrue(comentarioLargo.length() > 200);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario(comentarioLargo)
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("LONGITUD_EXCEDIDA"));
    }

    @Test
    @DisplayName("HU-03 Criterio 3: El comentario es completamente opcional (vacío o nulo permitido)")
    void registrarComentario_Opcional_PermiteVacio() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.NEUTRALIDAD);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario("   ")
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentario").doesNotExist())
                .andExpect(jsonPath("$.mensaje", containsString("omitido")));
    }

    @Test
    @DisplayName("HU-03 RNF Seguridad: Rechazo de etiquetas HTML o secuencias de inyección XSS")
    void registrarComentario_AtaqueXss_Rechazado() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ANSIEDAD);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario("<script>alert('xss')</script>")
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("SEGURIDAD_XSS_DETECTADA"));
    }

    @Test
    @DisplayName("HU-03 Moderación: Enmascaramiento de términos ofensivos inapropiados")
    void registrarComentario_ModeraContenidoOfensivo() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.ENFADO);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario("El conductor fue un imbecil y grosero.")
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentario").value("El conductor fue un *** y grosero."));
    }

    @Test
    @DisplayName("HU-03: Rechazo con HTTP 404 ante evento inexistente")
    void registrarComentario_EventoNoEncontrado_Retorna404() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idInexistente)
                .comentario("Comentario para evento fantasma")
                .build();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("REPORTE_NO_ENCONTRADO"));
    }

    @Test
    @DisplayName("HU-03 RNF Desempeño: Tiempo de respuesta inferior a 300 ms")
    void registrarComentario_DesempenoInferiorA300ms() throws Exception {
        UUID idEvento = registrarEmocionPrevia(TipoEmocion.FELICIDAD);

        RegistroComentarioSolicitud solicitud = RegistroComentarioSolicitud.builder()
                .idEvento(idEvento)
                .comentario("Todo muy tranquilo y seguro.")
                .build();

        long inicio = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/emociones/comentario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk());

        long duracion = System.currentTimeMillis() - inicio;
        assertTrue(duracion < 300, "El tiempo de procesamiento debe ser inferior a 300 ms, fue: " + duracion + " ms");
    }

    @Test
    @DisplayName("HU-03 Patrón Decorator: Enriquecimiento dinámico de EventoEmocion con commentContext")
    void patronDecorator_EnriqueceEventoSinRomperContrato() {
        UUID idEvento = UUID.randomUUID();
        Instant ahora = Instant.now();

        // 1. Evento Componente Base
        EventoEmocion eventoBase = new EventoEmocionBase(
                idEvento,
                TipoEmocion.FELICIDAD,
                "token-anonimo-12345",
                "8966c6c748fffff",
                "ZONA-PARQUE-CALDAS",
                ahora
        );

        assertEquals(idEvento, eventoBase.getIdEvento());
        assertEquals(TipoEmocion.FELICIDAD, eventoBase.getEmocion());
        assertFalse(eventoBase.getContextoDescriptivo().contains("commentContext"));

        // 2. Decorador Concreto de Comentario
        String texto = "Excelente ambiente en el centro histórico.";
        EventoEmocionConComentarioDecorador eventoDecorado = new EventoEmocionConComentarioDecorador(
                eventoBase,
                texto,
                ahora
        );

        // Cumple la misma interfaz EventoEmocion
        assertTrue(eventoDecorado instanceof EventoEmocion);
        assertEquals(idEvento, eventoDecorado.getIdEvento());
        assertEquals(TipoEmocion.FELICIDAD, eventoDecorado.getEmocion());
        assertEquals("8966c6c748fffff", eventoDecorado.getIdCeldaH3());

        // Atributos y comportamiento enriquecido por el decorador
        assertEquals(texto, eventoDecorado.getComentario());
        assertEquals(texto, eventoDecorado.getCommentContext());
        assertTrue(eventoDecorado.getContextoDescriptivo().contains("commentContext"));
        assertTrue(eventoDecorado.getContextoDescriptivo().contains(texto));
    }
}
