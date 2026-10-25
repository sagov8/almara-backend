package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.ActualizarResolucionH3Solicitud;
import com.almara.modules.admin.dto.CrearPoligonoUrbanoSolicitud;
import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.dto.ModificarPoligonoUrbanoSolicitud;
import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.models.PoligonoUrbanoEspacial;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioConfiguracionEspacialH3;
import com.almara.modules.admin.repositories.RepositorioPoligonoUrbanoEspacial;
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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorZonasEspacialesAdminPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioConfiguracionEspacialH3 repositorioConfiguracion;

    @Autowired
    private RepositorioPoligonoUrbanoEspacial repositorioPoligonos;

    @Autowired
    private RepositorioAuditoriaAdmin repositorioAuditoria;

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

        LoginAdminRespuesta loginResp = objectMapper.readValue(
                res.getResponse().getContentAsString(), LoginAdminRespuesta.class);
        this.tokenAdmin = loginResp.getToken();

        // Limpieza de polígono temporal si existía de pruebas anteriores
        repositorioPoligonos.findByIdZonaIgnoreCase("ZONA-TEST-HU14")
                .ifPresent(repositorioPoligonos::delete);
    }

    @Test
    @DisplayName("HU-14: Debe consultar la configuración vigente de resolución H3 exitosamente")
    void debeConsultarConfiguracionResolucionH3Exitosa() throws Exception {
        mockMvc.perform(get("/api/v1/admin/geo/resolucion")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("GLOBAL_POPAYAN")))
                .andExpect(jsonPath("$.resolucionMinima", is(7)))
                .andExpect(jsonPath("$.resolucionMaxima", is(10)))
                .andExpect(jsonPath("$.resolucionH3Default", greaterThanOrEqualTo(7)));
    }

    @Test
    @DisplayName("HU-14: Debe actualizar la resolución H3 por defecto y registrar en auditoría (<<include>>)")
    void debeActualizarResolucionH3DefaultYRegistrarAuditoria() throws Exception {
        int auditoriasAntes = repositorioAuditoria.findAll().size();

        ActualizarResolucionH3Solicitud solicitud = new ActualizarResolucionH3Solicitud(
                8,
                "Ajuste de agregación para macro-análisis territorial en Popayán"
        );

        mockMvc.perform(put("/api/v1/admin/geo/resolucion")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolucionH3Default", is(8)))
                .andExpect(jsonPath("$.actualizadoPor", is("admin@almara.com")));

        // Verificación del log de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoria.findAll();
        assertThat(auditorias.size()).isGreaterThan(auditoriasAntes);

        AuditoriaAdmin ultima = auditorias.get(auditorias.size() - 1);
        assertThat(ultima.getAccion()).isEqualTo("H3_RESOLUCION_ACTUALIZADA");
        assertThat(ultima.getActorResponsable()).isEqualTo("admin@almara.com");
        assertThat(ultima.getDetalle()).contains("Ajustada resolución H3 por defecto");
        assertThat(ultima.getDetalle()).contains("res=8");
    }

    @Test
    @DisplayName("HU-14: Debe rechazar resolución H3 fuera del rango operativo permitido (7 a 10)")
    void debeRechazarResolucionH3FueraDeLimitesOperativos() throws Exception {
        ActualizarResolucionH3Solicitud resolucionMuyBaja = new ActualizarResolucionH3Solicitud(
                6, // Menor a 7
                "Intento de resolución no permitida"
        );

        mockMvc.perform(put("/api/v1/admin/geo/resolucion")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolucionMuyBaja)))
                .andExpect(status().isBadRequest());

        ActualizarResolucionH3Solicitud resolucionMuyAlta = new ActualizarResolucionH3Solicitud(
                11, // Mayor a 10 (vulneraría RF-09 de anonimato)
                "Intento de resolución hiper-local insegura"
        );

        mockMvc.perform(put("/api/v1/admin/geo/resolucion")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolucionMuyAlta)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-14: Debe listar polígonos urbanos espaciales registrados en la base PostGIS")
    void debeListarPoligonosUrbanosRegistrados() throws Exception {
        mockMvc.perform(get("/api/v1/admin/geo/poligonos")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].idZona").exists())
                .andExpect(jsonPath("$[0].latitudCentroide").isNumber())
                .andExpect(jsonPath("$[0].longitudCentroide").isNumber())
                .andExpect(jsonPath("$[0].idCeldaH3Centroide").exists());
    }

    @Test
    @DisplayName("HU-14: Debe crear un polígono urbano con GeoJSON cerrado, calcular centroide e indexar H3 con auditoría")
    void debeCrearPoligonoUrbanoGeoJsonCerradoExitosoYRegistrarAuditoria() throws Exception {
        int auditoriasAntes = repositorioAuditoria.findAll().size();

        String geoJsonCerrado = "{\"type\":\"Polygon\",\"coordinates\":[[[-76.610,2.440],[-76.600,2.440],[-76.600,2.450],[-76.610,2.450],[-76.610,2.440]]]}";

        CrearPoligonoUrbanoSolicitud solicitud = new CrearPoligonoUrbanoSolicitud(
                "ZONA-TEST-HU14",
                "Zona Piloto Universidad del Cauca",
                "Perímetro de prueba académica y residencial",
                geoJsonCerrado,
                9,
                "Inclusión de zona experimental de monitorización territorial"
        );

        mockMvc.perform(post("/api/v1/admin/geo/poligonos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idZona", is("ZONA-TEST-HU14")))
                .andExpect(jsonPath("$.nombreZona", is("Zona Piloto Universidad del Cauca")))
                .andExpect(jsonPath("$.resolucionH3", is(9)))
                .andExpect(jsonPath("$.latitudCentroide", closeTo(2.445, 0.0001)))
                .andExpect(jsonPath("$.longitudCentroide", closeTo(-76.605, 0.0001)))
                .andExpect(jsonPath("$.idCeldaH3Centroide").exists())
                .andExpect(jsonPath("$.totalCeldasH3Cubiertas", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activo", is(true)));

        // Verificación de persistencia en base de datos
        assertThat(repositorioPoligonos.existsByIdZonaIgnoreCase("ZONA-TEST-HU14")).isTrue();

        // Verificación de bitácora de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoria.findAll();
        assertThat(auditorias.size()).isGreaterThan(auditoriasAntes);
        AuditoriaAdmin ultima = auditorias.get(auditorias.size() - 1);
        assertThat(ultima.getAccion()).isEqualTo("POLIGONO_URBANO_CREADO");
        assertThat(ultima.getDetalle()).contains("ZONA-TEST-HU14");
    }

    @Test
    @DisplayName("HU-14: Debe rechazar geometría si el polígono GeoJSON no está cerrado (primer punto != último punto)")
    void debeRechazarPoligonoGeoJsonNoCerrado() throws Exception {
        // Coordenadas abiertas (inicio [-76.610, 2.440] != fin [-76.615, 2.455])
        String geoJsonAbierto = "{\"type\":\"Polygon\",\"coordinates\":[[[-76.610,2.440],[-76.600,2.440],[-76.600,2.450],[-76.615,2.455]]]}";

        CrearPoligonoUrbanoSolicitud solicitudInvalida = new CrearPoligonoUrbanoSolicitud(
                "ZONA-ABIERTA",
                "Zona Invalida",
                "Prueba de polígono no cerrado",
                geoJsonAbierto,
                9,
                "Intento de registrar geometría abierta"
        );

        mockMvc.perform(post("/api/v1/admin/geo/poligonos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudInvalida)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-14: Debe impedir duplicidad en el identificador de la zona (Conflict 409)")
    void debeImpedirDuplicidadDeIdZona() throws Exception {
        String geoJsonCerrado = "{\"type\":\"Polygon\",\"coordinates\":[[[-76.608,2.441],[-76.603,2.441],[-76.603,2.447],[-76.608,2.447],[-76.608,2.441]]]}";

        // Intentar registrar con ID preexistente de semilla 'ZONA-COMUNA-4-CENTRO'
        CrearPoligonoUrbanoSolicitud duplicada = new CrearPoligonoUrbanoSolicitud(
                "ZONA-COMUNA-4-CENTRO",
                "Intento Duplicado",
                "No debe permitirse",
                geoJsonCerrado,
                9,
                "Test de duplicidad"
        );

        mockMvc.perform(post("/api/v1/admin/geo/poligonos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicada)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("HU-14: Debe modificar un polígono urbano recalculando el centroide e indexación espacial")
    void debeModificarPoligonoUrbanoYRecalcularCentroide() throws Exception {
        String nuevoGeoJson = "{\"type\":\"Polygon\",\"coordinates\":[[[-76.605,2.442],[-76.601,2.442],[-76.601,2.448],[-76.605,2.448],[-76.605,2.442]]]}";

        ModificarPoligonoUrbanoSolicitud modSolicitud = new ModificarPoligonoUrbanoSolicitud(
                "Comuna 4 - Centro Histórico Ampliado",
                "Ajuste limítrofe por expansión patrimonial hacia La Ermita",
                nuevoGeoJson,
                9,
                true,
                "Ajuste cartográfico oficial del POT de Popayán"
        );

        mockMvc.perform(put("/api/v1/admin/geo/poligonos/ZONA-COMUNA-4-CENTRO")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modSolicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreZona", is("Comuna 4 - Centro Histórico Ampliado")))
                .andExpect(jsonPath("$.latitudCentroide", closeTo(2.445, 0.0001)))
                .andExpect(jsonPath("$.longitudCentroide", closeTo(-76.603, 0.0001)));

        // Verificación de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoria.findAll();
        AuditoriaAdmin ultima = auditorias.get(auditorias.size() - 1);
        assertThat(ultima.getAccion()).isEqualTo("POLIGONO_URBANO_MODIFICADO");
        assertThat(ultima.getDetalle()).contains("ZONA-COMUNA-4-CENTRO");
    }

    @Test
    @DisplayName("HU-14: Debe desactivar un polígono urbano mediante borrado lógico y registrar en auditoría")
    void debeDesactivarPoligonoUrbanoConBorradoLogicoYAuditoria() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/geo/poligonos/ZONA-COMUNA-2-NORTE")
                        .param("justificacion", "Reestructuración de cuadrantes del sector norte")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idZona", is("ZONA-COMUNA-2-NORTE")))
                .andExpect(jsonPath("$.activo", is(false)));

        PoligonoUrbanoEspacial entidad = repositorioPoligonos.findByIdZonaIgnoreCase("ZONA-COMUNA-2-NORTE").orElseThrow();
        assertThat(entidad.isActivo()).isFalse();

        // Verificación de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoria.findAll();
        AuditoriaAdmin ultima = auditorias.get(auditorias.size() - 1);
        assertThat(ultima.getAccion()).isEqualTo("POLIGONO_URBANO_DESACTIVADO");
        assertThat(ultima.getDetalle()).contains("ZONA-COMUNA-2-NORTE");
    }

    @Test
    @DisplayName("HU-14: Debe denegar el acceso a la gestión espacial si no se cuenta con rol ROLE_ADMIN")
    void debeDenegarAccesoSinRolAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/geo/resolucion"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/geo/poligonos"))
                .andExpect(status().isForbidden());
    }
}
