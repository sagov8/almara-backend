package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para la consulta del Catálogo de Planes SaaS (HU-19).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorPlanesSaaSPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioClienteInstitucional repositorioClienteInstitucional;

    @Autowired
    private RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;

    @Autowired
    private RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;

    @BeforeEach
    void setUp() {
        repositorioAuditoriaInstitucional.deleteAll();
        repositorioSuscripcionInstitucional.deleteAll();
        repositorioClienteInstitucional.deleteAll();
    }

    private String registrarYLoguearCliente(String razonSocial, String nit, String correo, String password) throws Exception {
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial(razonSocial)
                .identificacionFiscalNit(nit)
                .nombreContacto("Secretario de Salud")
                .correoCorporativo(correo)
                .password(password)
                .sectorActividad("GOBIERNO")
                .build();

        String respRegistro = mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registro)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String tokenVerificacion = objectMapper.readTree(respRegistro).get("tokenVerificacionSimulado").asText();

        mockMvc.perform(post("/api/v1/institutional/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificacionCorreoSolicitud(tokenVerificacion))))
                .andExpect(status().isOk());

        LoginInstitucionalSolicitud login = LoginInstitucionalSolicitud.builder()
                .correoInstitucional(correo)
                .password(password)
                .build();

        String respLogin = mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respLogin).get("tokenAcceso").asText();
    }

    @Test
    @DisplayName("HU-19 Criterios 1, 2, 3 y Desempeño: Consulta catálogo activo en < 500ms con desglose de módulos")
    void debeConsultarCatalogoDePlanesActivosEnMenosDe500msConSusModulos() throws Exception {
        long inicio = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/institutional/planes/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlanesActivos", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_FREE')].esGratuito", contains(true)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_FREE')].precioFinal", contains(0.0)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_STANDARD')].precioFinal", contains(149000.0)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_ENTERPRISE')].precioFinal", contains(1490000.0)))
                .andExpect(jsonPath("$.planes[0].modulos", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.planes[0].modulos[*].codigoModulo", hasItems(
                        "MOD_DASHBOARD_BASICO",
                        "MOD_FILTROS_TERRITORIALES",
                        "MOD_EXPORTACION_REPORTES"
                )));

        long duracionMs = System.currentTimeMillis() - inicio;
        assertTrue(duracionMs < 500, "El tiempo de respuesta del catálogo debe ser inferior a 500 ms (fue " + duracionMs + " ms)");
    }

    @Test
    @DisplayName("HU-19 Criterio 4: Identifica el plan actual del cliente cuando se envía token Bearer")
    void debeDestacarElPlanActualCuandoElClienteInstitucionalEnviaTokenDeSesion() throws Exception {
        String tokenJwt = registrarYLoguearCliente(
                "Hospital Universitario San José",
                "891500456-9",
                "gerencia@hospitalsanjose.gov.co",
                "SanJose2026*Clave"
        );

        mockMvc.perform(get("/api/v1/institutional/planes/catalogo")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPlanActualCliente", is("PLAN_FREE")))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_FREE')].esPlanActual", contains(true)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_STANDARD')].esPlanActual", contains(false)))
                .andExpect(jsonPath("$.planes[?(@.codigo == 'PLAN_ENTERPRISE')].esPlanActual", contains(false)));
    }
}
