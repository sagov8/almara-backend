package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.AprobarInstitucionSolicitud;
import com.almara.modules.admin.dto.DarDeBajaInstitucionSolicitud;
import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.dto.ReactivarInstitucionSolicitud;
import com.almara.modules.admin.dto.RevocarSesionesInstitucionSolicitud;
import com.almara.modules.admin.dto.SuspenderInstitucionSolicitud;
import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.security.ServicioRevocacionToken;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorGestionInstitucionalAdminPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioClienteInstitucional repositorioClienteInstitucional;

    @Autowired
    private RepositorioAuditoriaAdmin repositorioAuditoriaAdmin;

    @Autowired
    private ServicioRevocacionToken servicioRevocacionToken;

    @Autowired
    private ServicioNotificacionPort servicioNotificacionPort;

    private String tokenAdminValido;

    // UUIDs sembrados en migración V9
    private final UUID idPopayanPendiente = UUID.fromString("55555555-5555-5555-5555-555555555501");
    private final UUID idUnicaucaActiva = UUID.fromString("55555555-5555-5555-5555-555555555502");
    private final UUID idCamaraComercioSuspendida = UUID.fromString("55555555-5555-5555-5555-555555555503");
    private final UUID idHospitalSanJose = UUID.fromString("55555555-5555-5555-5555-555555555504");

    @BeforeEach
    void setUp() throws Exception {
        tokenAdminValido = obtenerTokenAdmin();
    }

    private String obtenerTokenAdmin() throws Exception {
        LoginAdminSolicitud login = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");
        MvcResult res = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        LoginAdminRespuesta loginRes = objectMapper.readValue(res.getResponse().getContentAsString(), LoginAdminRespuesta.class);
        return loginRes.getToken();
    }

    @Test
    @DisplayName("GET /api/v1/admin/instituciones: Lista instituciones con datos completos y planes asignados")
    void listarInstituciones_conTokenAdmin_retornaListaCompletaConPlanes() throws Exception {
        mockMvc.perform(get("/api/v1/admin/instituciones")
                        .header("Authorization", "Bearer " + tokenAdminValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[*].razonSocial", hasItem(containsString("Alcaldía de Popayán"))))
                .andExpect(jsonPath("$[*].razonSocial", hasItem(containsString("Universidad del Cauca"))))
                .andExpect(jsonPath("$[*].nombrePlan", hasItem(notNullValue())));
    }

    @Test
    @DisplayName("GET /api/v1/admin/instituciones?estado=ACTIVA: Filtra únicamente instituciones activas")
    void listarInstituciones_conFiltroEstado_filtraCorrectamente() throws Exception {
        mockMvc.perform(get("/api/v1/admin/instituciones")
                        .param("estado", "ACTIVA")
                        .header("Authorization", "Bearer " + tokenAdminValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estadoCuenta", everyItem(is("ACTIVA"))));
    }

    @Test
    @DisplayName("GET /api/v1/admin/instituciones/{id}: Retorna el detalle institucional por ID")
    void obtenerDetalle_conIdExistente_retornaDetalleInstitucional() throws Exception {
        mockMvc.perform(get("/api/v1/admin/instituciones/" + idUnicaucaActiva)
                        .header("Authorization", "Bearer " + tokenAdminValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInstitucion", is(idUnicaucaActiva.toString())))
                .andExpect(jsonPath("$.razonSocial", containsString("Universidad del Cauca")))
                .andExpect(jsonPath("$.estadoCuenta", is("ACTIVA")))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_STANDARD")));
    }

    @Test
    @DisplayName("GET /api/v1/admin/instituciones/planes: Lista el catálogo de planes para asignación institucional")
    void listarPlanes_retornaPlanesDisponibles() throws Exception {
        mockMvc.perform(get("/api/v1/admin/instituciones/planes")
                        .header("Authorization", "Bearer " + tokenAdminValido))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[*].codigo", hasItems("PLAN_FREE", "PLAN_STANDARD", "PLAN_ENTERPRISE")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/aprobar: Aprueba cuenta pendiente, asocia plan y audita")
    void aprobarInstitucion_estadoPendiente_transicionaAActivaYAsignaPlanYNotificaYAudita() throws Exception {
        AprobarInstitucionSolicitud solicitud = new AprobarInstitucionSolicitud(
                null, "PLAN_ENTERPRISE", "Aprobación formal por convenio territorial Popayán Inteligente"
        );

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idPopayanPendiente + "/aprobar")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idInstitucion", is(idPopayanPendiente.toString())))
                .andExpect(jsonPath("$.estadoCuenta", is("ACTIVA")))
                .andExpect(jsonPath("$.correoVerificado", is(true)))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_ENTERPRISE")));

        // Verificar persistencia en base de datos
        ClienteInstitucional actualizado = repositorioClienteInstitucional.findById(idPopayanPendiente).orElseThrow();
        assertEquals(EstadoCuentaInstitucional.ACTIVA.name(), actualizado.getEstadoCuenta());
        assertTrue(actualizado.isCorreoVerificado());

        // Verificar log de auditoría
        List<AuditoriaAdmin> logs = repositorioAuditoriaAdmin.findByAccionOrderByFechaEventoDesc("INSTITUCION_APROBADA");
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getDetalle().contains("Alcaldía de Popayán"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/aprobar: Falla con 409 Conflict si la cuenta ya está activa (Patrón Estado)")
    void aprobarInstitucion_cuandoYaEstaActiva_lanzaConflicto409PorPatronEstado() throws Exception {
        AprobarInstitucionSolicitud solicitud = new AprobarInstitucionSolicitud(null, "PLAN_STANDARD", "Reintento inválido");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idUnicaucaActiva + "/aprobar")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("TRANSICION_ESTADO_INVALIDA")));
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/suspender: Suspende cuenta activa, revoca sesiones y audita")
    void suspenderInstitucion_conMotivoValido_transicionaASuspendidaYRevocaSesionesYAudita() throws Exception {
        SuspenderInstitucionSolicitud solicitud = new SuspenderInstitucionSolicitud("Falta de pago de mensualidad estándar de octubre");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idUnicaucaActiva + "/suspender")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCuenta", is("SUSPENDIDA")));

        // Verificar que las sesiones del usuario fueron revocadas en la blacklist
        long ahoraSegundos = Instant.now().getEpochSecond();
        assertTrue(servicioRevocacionToken.estaSesionUsuarioRevocada(idUnicaucaActiva, ahoraSegundos - 10));

        // Verificar log de auditoría
        List<AuditoriaAdmin> logs = repositorioAuditoriaAdmin.findByAccionOrderByFechaEventoDesc("INSTITUCION_SUSPENDIDA");
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getDetalle().contains("Universidad del Cauca"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/suspender: Falla con 400 si falta el motivo")
    void suspenderInstitucion_sinMotivo_fallaValidacion400() throws Exception {
        SuspenderInstitucionSolicitud solicitud = new SuspenderInstitucionSolicitud("");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idUnicaucaActiva + "/suspender")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/dar-de-baja: Da de baja definitiva a cuenta institucional")
    void darDeBajaInstitucion_conMotivo_transicionaAInactivaYRevocaSesionesYAudita() throws Exception {
        DarDeBajaInstitucionSolicitud solicitud = new DarDeBajaInstitucionSolicitud("Cancelación voluntaria de contrato institucional");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idHospitalSanJose + "/dar-de-baja")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCuenta", is("INACTIVA")))
                .andExpect(jsonPath("$.activo", is(false)));

        // Verificar log de auditoría
        List<AuditoriaAdmin> logs = repositorioAuditoriaAdmin.findByAccionOrderByFechaEventoDesc("INSTITUCION_DADA_DE_BAJA");
        assertFalse(logs.isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/reactivar: Reactiva cuenta suspendida a estado ACTIVA")
    void reactivarInstitucion_cuentaSuspendida_transicionaAActivaYAudita() throws Exception {
        ReactivarInstitucionSolicitud solicitud = new ReactivarInstitucionSolicitud("Pago regularizado por parte de la Cámara de Comercio");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idCamaraComercioSuspendida + "/reactivar")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCuenta", is("ACTIVA")))
                .andExpect(jsonPath("$.activo", is(true)));

        List<AuditoriaAdmin> logs = repositorioAuditoriaAdmin.findByAccionOrderByFechaEventoDesc("INSTITUCION_REACTIVADA");
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).getDetalle().contains("Cámara de Comercio"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/instituciones/{id}/revocar-sesiones: Ejecución forzosa de revocación de sesiones")
    void revocarSesionesActivas_fuerzaRevocacionInmediataEnListaNegraYAudita() throws Exception {
        RevocarSesionesInstitucionSolicitud solicitud = new RevocarSesionesInstitucionSolicitud("Sospecha preventiva de vulneración de credenciales");

        mockMvc.perform(post("/api/v1/admin/instituciones/" + idUnicaucaActiva + "/revocar-sesiones")
                        .header("Authorization", "Bearer " + tokenAdminValido)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje", containsString("revocados exitosamente")));

        long ahoraSegundos = Instant.now().getEpochSecond();
        assertTrue(servicioRevocacionToken.estaSesionUsuarioRevocada(idUnicaucaActiva, ahoraSegundos));

        List<AuditoriaAdmin> logs = repositorioAuditoriaAdmin.findByAccionOrderByFechaEventoDesc("SESIONES_INSTITUCION_REVOCADAS");
        assertFalse(logs.isEmpty());
    }

    @Test
    @DisplayName("Seguridad: Petición sin token administrativo retorna 403 Forbidden")
    void endpointsInstitucionales_sinToken_retorna403Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/instituciones"))
                .andExpect(status().isForbidden());
    }
}
