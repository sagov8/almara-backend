package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.ActualizarUmbralMapaSolicitud;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.models.ConfiguracionMapaUmbral;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioConfiguracionMapaUmbral;
import com.almara.modules.admin.services.ProveedorConfiguracionUmbralMapa;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorUmbralesMapaAdminPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioConfiguracionMapaUmbral repositorioConfiguracion;

    @Autowired
    private RepositorioAuditoriaAdmin repositorioAuditoria;

    @Autowired
    private ProveedorConfiguracionUmbralMapa proveedorConfiguracion;

    private String tokenAdmin;

    @BeforeEach
    void setUp() throws Exception {
        // Autenticación administrativa de prueba
        LoginAdminSolicitud loginReq = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");
        MvcResult res = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        com.almara.modules.admin.dto.LoginAdminRespuesta loginResp = objectMapper.readValue(
                res.getResponse().getContentAsString(), com.almara.modules.admin.dto.LoginAdminRespuesta.class);
        this.tokenAdmin = loginResp.getToken();

        // Restablecer parámetros base antes de cada prueba
        repositorioConfiguracion.obtenerConfiguracionGlobal().ifPresent(c -> {
            c.setUmbralMinimoK(5);
            c.setTiempoRecalculoSegundos(60);
            c.setModoEstricto(true);
            repositorioConfiguracion.save(c);
            proveedorConfiguracion.propagarNuevaConfiguracion(c);
        });
    }

    @Test
    @DisplayName("HU-13: Debe consultar la configuración vigente de umbrales del mapa exitosamente")
    void debeConsultarConfiguracionVigenteDeUmbralesExitosa() throws Exception {
        mockMvc.perform(get("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("GLOBAL_POPAYAN")))
                .andExpect(jsonPath("$.umbralMinimoK", is(5)))
                .andExpect(jsonPath("$.tiempoRecalculoSegundos", is(60)))
                .andExpect(jsonPath("$.modoEstricto", is(true)))
                .andExpect(jsonPath("$.minimoTecnicoPermitidoK", is(5)));
    }

    @Test
    @DisplayName("HU-13: Debe denegar el acceso a la configuración si no se provee token con rol ROLE_ADMIN")
    void debeDenegarAccesoSiFaltaTokenAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/mapa/umbrales"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("HU-13: Debe actualizar en caliente los umbrales de privacidad y registrar log de auditoría (<<include>>)")
    void debeActualizarConfiguracionDeUmbralesEnCalienteYRegistrarAuditoria() throws Exception {
        int registrosAuditoriaAntes = repositorioAuditoria.findAll().size();

        ActualizarUmbralMapaSolicitud solicitud = new ActualizarUmbralMapaSolicitud(
                15,
                120,
                true,
                "Protección en Comunidad reforzada para Popayán.",
                "Calibración de privacidad preventiva por afluencia en Semana Santa"
        );

        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.umbralMinimoK", is(15)))
                .andExpect(jsonPath("$.tiempoRecalculoSegundos", is(120)))
                .andExpect(jsonPath("$.modoEstricto", is(true)))
                .andExpect(jsonPath("$.actualizadoPor", is("admin@almara.com")));

        // Verificación en caliente en el proveedor de memoria (O(1))
        assertThat(proveedorConfiguracion.obtenerUmbralMinimoK()).isEqualTo(15);

        // Verificación del log de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoria.findAll();
        assertThat(auditorias.size()).isGreaterThan(registrosAuditoriaAntes);

        AuditoriaAdmin ultimaAuditoria = auditorias.get(auditorias.size() - 1);
        assertThat(ultimaAuditoria.getAccion()).isEqualTo("UMBRAL_MAPA_ACTUALIZADO");
        assertThat(ultimaAuditoria.getActorResponsable()).isEqualTo("admin@almara.com");
        assertThat(ultimaAuditoria.getDetalle()).contains("Ajustado umbral k-anonimato de k=5 a k=15");
        assertThat(ultimaAuditoria.getDetalle()).contains("Semana Santa");
    }

    @Test
    @DisplayName("HU-13: Debe impedir configurar un umbral k menor al límite técnico de privacidad (k < 5)")
    void debeImpedirConfigurarUmbralMenorAlMinimoTecnicoDeSeguridad() throws Exception {
        ActualizarUmbralMapaSolicitud solicitudInvalida = new ActualizarUmbralMapaSolicitud(
                3, // Inválido: menor a 5
                60,
                true,
                "Mensaje de protección",
                "Intento de vulnerar el límite mínimo técnico"
        );

        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-13: Debe impedir configurar un tiempo de recálculo menor a 10 segundos")
    void debeImpedirTiempoRecalculoInvalido() throws Exception {
        ActualizarUmbralMapaSolicitud solicitudInvalida = new ActualizarUmbralMapaSolicitud(
                10,
                5, // Inválido: menor a 10s
                true,
                "Mensaje de prueba",
                "Frecuencia no soportada"
        );

        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-13: Debe exigir una justificación técnica para la auditoría")
    void debeExigirJustificacionDeAuditoria() throws Exception {
        ActualizarUmbralMapaSolicitud sinJustificacion = new ActualizarUmbralMapaSolicitud(
                10,
                60,
                true,
                "Mensaje de prueba",
                "   " // Vacía
        );

        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinJustificacion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-13: Debe restablecer los umbrales por defecto y registrar en auditoría")
    void debeRestablecerUmbralesPorDefectoConAuditoria() throws Exception {
        // Primero cambiamos a k=25
        ActualizarUmbralMapaSolicitud previa = new ActualizarUmbralMapaSolicitud(
                25, 90, true, "Mensaje", "Ajuste temporal previo"
        );
        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(previa)))
                .andExpect(status().isOk());

        assertThat(proveedorConfiguracion.obtenerUmbralMinimoK()).isEqualTo(25);

        // Ahora restablecemos
        mockMvc.perform(post("/api/v1/admin/mapa/umbrales/restablecer")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("justificacion", "Retorno al estado base normal"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.umbralMinimoK", is(5)))
                .andExpect(jsonPath("$.tiempoRecalculoSegundos", is(60)));

        assertThat(proveedorConfiguracion.obtenerUmbralMinimoK()).isEqualTo(5);
    }

    @Test
    @DisplayName("HU-13: El mapa público debe consultar la configuración de privacidad vigente")
    void debeConsultarConfiguracionPublicaDelMapa() throws Exception {
        mockMvc.perform(get("/api/v1/mapa/configuracion-privacidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.umbralMinimoK", is(5)))
                .andExpect(jsonPath("$.tiempoRecalculoSegundos", is(60)))
                .andExpect(jsonPath("$.modoEstricto", is(true)));
    }

    @Test
    @DisplayName("HU-13 / RF-23: Modificar umbral en caliente debe ocultar celdas que no cumplan el nuevo k en /api/v1/mapa/celdas")
    void debeAfectarVisualizacionDeCeldasAlModificarUmbralEnCaliente() throws Exception {
        // Con k=5 obtenemos las celdas visibles
        MvcResult resK5 = mockMvc.perform(get("/api/v1/mapa/celdas?zoom=14"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootK5 = objectMapper.readTree(resK5.getResponse().getContentAsString());
        int totalCeldasK5 = rootK5.get("totalCeldasVisibles").asInt();
        assertThat(totalCeldasK5).isGreaterThan(0);

        // Elevamos el umbral a k=500 (ninguna celda local tiene tantos reportes sembrados)
        ActualizarUmbralMapaSolicitud altaSeguridad = new ActualizarUmbralMapaSolicitud(
                500,
                60,
                true,
                "Privacidad extrema temporal",
                "Simulación de máxima reserva ciudadana"
        );

        mockMvc.perform(put("/api/v1/admin/mapa/umbrales")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(altaSeguridad)))
                .andExpect(status().isOk());

        // Consultamos nuevamente el mapa sin reiniciar el backend (en caliente)
        MvcResult resK500 = mockMvc.perform(get("/api/v1/mapa/celdas?zoom=14"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode rootK500 = objectMapper.readTree(resK500.getResponse().getContentAsString());
        int totalCeldasK500 = rootK500.get("totalCeldasVisibles").asInt();

        // RF-23: Con umbral 500, las celdas se ocultan protegiendo el anonimato
        assertThat(totalCeldasK500).isEqualTo(0);
        assertThat(rootK500.get("umbralMinimoAplicado").asInt()).isEqualTo(500);
    }
}
