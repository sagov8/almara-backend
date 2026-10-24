package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.CrearEmocionSolicitud;
import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.dto.ModificarEmocionSolicitud;
import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorCatalogoEmocionesAdminPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioCatalogoEmocion repositorioCatalogoEmocion;

    @Autowired
    private RepositorioAuditoriaAdmin repositorioAuditoriaAdmin;

    private String tokenAdmin;

    @BeforeEach
    void setUp() throws Exception {
        // Eliminar cualquier emoción temporal creada por pruebas
        if (repositorioCatalogoEmocion.existsById("CALMA_TEST")) {
            repositorioCatalogoEmocion.deleteById("CALMA_TEST");
        }

        // Asegurar que las emociones base estén activas antes de cada prueba
        repositorioCatalogoEmocion.findAll().forEach(e -> {
            e.setActivo(true);
            repositorioCatalogoEmocion.save(e);
        });

        // Obtener token administrativo válido para admin@almara.com
        LoginAdminSolicitud login = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        LoginAdminRespuesta loginResp = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), LoginAdminRespuesta.class);
        tokenAdmin = loginResp.getToken();
    }

    @Test
    @DisplayName("HU-12: Listado completo de emociones del catálogo para el Administrador")
    void debeListarEmocionesDelCatalogoExitosamenteParaAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/emociones")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].idEmocion").exists())
                .andExpect(jsonPath("$[0].nombreEtiqueta").exists())
                .andExpect(jsonPath("$[0].codigoHexColor").exists());
    }

    @Test
    @DisplayName("HU-12: Rechazo de acceso a la gestión del catálogo sin token de autenticación")
    void debeRechazarAccesoSinTokenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/emociones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTORIZACION_REQUERIDA"));
    }

    @Test
    @DisplayName("HU-12: Caso de uso <<include>> Agregar emoción al catálogo y registrar log de auditoría")
    void debeAgregarNuevaEmocionPredeterminadaYRegistrarAuditoria() throws Exception {
        String idTest = "CALMA_TEST";
        if (repositorioCatalogoEmocion.existsById(idTest)) {
            repositorioCatalogoEmocion.deleteById(idTest);
        }

        CrearEmocionSolicitud solicitud = new CrearEmocionSolicitud(
                idTest,
                "Calma Urbana",
                "#3B82F6",
                "#DBEAFE",
                "😌",
                "Sensación de tranquilidad y balance interior"
        );

        mockMvc.perform(post("/api/v1/admin/emociones")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEmocion").value(idTest))
                .andExpect(jsonPath("$.nombreEtiqueta").value("Calma Urbana"))
                .andExpect(jsonPath("$.codigoHexColor").value("#3B82F6"))
                .andExpect(jsonPath("$.activo").value(true));

        // Validar en base de datos
        EntidadCatalogoEmocion enDb = repositorioCatalogoEmocion.findById(idTest).orElseThrow();
        assertThat(enDb.getNombreEtiqueta()).isEqualTo("Calma Urbana");
        assertThat(enDb.getActivo()).isTrue();

        // Validar registro en bitácora de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "CATALOGO_AGREGAR_EMOCION".equals(a.getAccion())
                && a.getDetalle().contains(idTest));
    }

    @Test
    @DisplayName("HU-12: Rechazar adición de emoción si el identificador clave ya existe (Conflicto)")
    void debeRechazarAgregarEmocionConIdDuplicado() throws Exception {
        CrearEmocionSolicitud solicitudDuplicada = new CrearEmocionSolicitud(
                "FELICIDAD",
                "Otra Felicidad",
                "#10B981",
                "#D1FAE5",
                "😊",
                "Duplicada"
        );

        mockMvc.perform(post("/api/v1/admin/emociones")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudDuplicada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMOCION_YA_EXISTE"));
    }

    @Test
    @DisplayName("HU-12: Validación estricta del formato de color hexadecimal (#RRGGBB)")
    void debeRechazarColorConFormatoInvalido() throws Exception {
        CrearEmocionSolicitud solicitudColorMalo = new CrearEmocionSolicitud(
                "COLOR_MALO",
                "Color Malo",
                "azul_invalido",
                "#000000",
                "❓",
                "Invalido"
        );

        mockMvc.perform(post("/api/v1/admin/emociones")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudColorMalo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION_FALLIDA"));
    }

    @Test
    @DisplayName("HU-12: Caso de uso <<include>> Modificar emoción preexistente manteniendo inmutable su ID")
    void debeModificarEmocionExistenteManteniendoInmutableSuClave() throws Exception {
        ModificarEmocionSolicitud solicitud = new ModificarEmocionSolicitud(
                "Felicidad Suprema",
                "#059669",
                "#A7F3D0",
                "😄",
                "Alegría desbordante y máxima satisfacción"
        );

        mockMvc.perform(put("/api/v1/admin/emociones/FELICIDAD")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmocion").value("FELICIDAD"))
                .andExpect(jsonPath("$.nombreEtiqueta").value("Felicidad Suprema"))
                .andExpect(jsonPath("$.codigoHexColor").value("#059669"))
                .andExpect(jsonPath("$.iconoSvg").value("😄"));

        // Validar registro en auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "CATALOGO_MODIFICAR_EMOCION".equals(a.getAccion())
                && a.getDetalle().contains("FELICIDAD"));
    }

    @Test
    @DisplayName("HU-12: Caso de uso <<include>> Desactivar emoción por borrado lógico usando State Pattern")
    void debeDesactivarEmocionMedianteBorradoLogicoYRegistrarAuditoria() throws Exception {
        // Asegurar que hay al menos 4 emociones activas antes de desactivar
        assertThat(repositorioCatalogoEmocion.countByActivoTrue()).isGreaterThanOrEqualTo(4);

        mockMvc.perform(patch("/api/v1/admin/emociones/ANSIEDAD/desactivar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmocion").value("ANSIEDAD"))
                .andExpect(jsonPath("$.activo").value(false));

        EntidadCatalogoEmocion enDb = repositorioCatalogoEmocion.findById("ANSIEDAD").orElseThrow();
        assertThat(enDb.getActivo()).isFalse();

        // Validar registro en auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "CATALOGO_DESACTIVAR_EMOCION".equals(a.getAccion())
                && a.getDetalle().contains("ANSIEDAD"));
    }

    @Test
    @DisplayName("HU-12: Regla RF-05 - Impedir desactivación si el total de emociones activas restantes es inferior a 3")
    void debeImpedirDesactivarSiQuedanMenosDeTresEmocionesActivas() throws Exception {
        // Desactivamos de forma controlada hasta dejar exactamente 3 activas
        List<EntidadCatalogoEmocion> todas = repositorioCatalogoEmocion.findAll();
        long activasDeseadas = 3;
        long activasActuales = repositorioCatalogoEmocion.countByActivoTrue();
        for (EntidadCatalogoEmocion e : todas) {
            if (activasActuales > activasDeseadas && !"FELICIDAD".equals(e.getIdEmocion())) {
                e.setActivo(false);
                repositorioCatalogoEmocion.save(e);
                activasActuales--;
            }
        }

        assertThat(repositorioCatalogoEmocion.countByActivoTrue()).isEqualTo(3L);

        // Intentar desactivar una cuarta emoción cuando solo quedan 3 debe arrojar BAD_REQUEST
        mockMvc.perform(patch("/api/v1/admin/emociones/FELICIDAD/desactivar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CATALOGO_MINIMO_REQUERIDO"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("al menos 3 emociones activas")));

        // La emoción debe seguir activa
        EntidadCatalogoEmocion enDb = repositorioCatalogoEmocion.findById("FELICIDAD").orElseThrow();
        assertThat(enDb.getActivo()).isTrue();
    }

    @Test
    @DisplayName("HU-12: Reactivar emoción inactiva en el catálogo")
    void debeReactivarEmocionInactiva() throws Exception {
        // Ponemos ANSIEDAD como inactiva primero
        EntidadCatalogoEmocion ansiedad = repositorioCatalogoEmocion.findById("ANSIEDAD").orElseThrow();
        ansiedad.setActivo(false);
        repositorioCatalogoEmocion.save(ansiedad);

        mockMvc.perform(patch("/api/v1/admin/emociones/ANSIEDAD/activar")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEmocion").value("ANSIEDAD"))
                .andExpect(jsonPath("$.activo").value(true));

        EntidadCatalogoEmocion enDb = repositorioCatalogoEmocion.findById("ANSIEDAD").orElseThrow();
        assertThat(enDb.getActivo()).isTrue();

        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "CATALOGO_ACTIVAR_EMOCION".equals(a.getAccion()));
    }
}
