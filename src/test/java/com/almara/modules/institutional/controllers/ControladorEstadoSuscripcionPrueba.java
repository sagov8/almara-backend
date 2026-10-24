package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
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

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para la consulta del Estado de Suscripción Institucional (HU-21).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorEstadoSuscripcionPrueba {

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

    private String[] registrarYLoguearCliente(String razonSocial, String nit, String correo, String password) throws Exception {
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial(razonSocial)
                .identificacionFiscalNit(nit)
                .nombreContacto("Director Administrativo")
                .correoCorporativo(correo)
                .password(password)
                .sectorActividad("EDUCACION")
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

        String tokenAcceso = objectMapper.readTree(respLogin).get("tokenAcceso").asText();
        String idInstitucion = objectMapper.readTree(respLogin).get("idInstitucion").asText();

        return new String[]{tokenAcceso, idInstitucion};
    }

    @Test
    @DisplayName("HU-21 Criterio 1, 2 y Desempeño: Consulta suscripción activa en < 400ms con aislamiento y módulos")
    void debeConsultarEstadoDeSuscripcionActivaEnMenosDe400ms() throws Exception {
        String[] authData = registrarYLoguearCliente(
                "Colegio Mayor del Cauca",
                "891501789-2",
                "rectoria@colmayor.edu.co",
                "ColMayor2026*Clave"
        );
        String tokenJwt = authData[0];

        long inicio = System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/institutional/suscripciones/mi-estado")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial", is("Colegio Mayor del Cauca")))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_FREE")))
                .andExpect(jsonPath("$.estadoSuscripcion", is("ACTIVA")))
                .andExpect(jsonPath("$.vigente", is(true)))
                .andExpect(jsonPath("$.diasRestantes", greaterThanOrEqualTo(29)))
                .andExpect(jsonPath("$.modulosHabilitados", hasItem("MOD_DASHBOARD_BASICO")))
                .andExpect(jsonPath("$.modulosRestringidos", hasItems("MOD_FILTROS_TERRITORIALES", "MOD_EXPORTACION_REPORTES")));

        long duracionMs = System.currentTimeMillis() - inicio;
        assertTrue(duracionMs < 400, "El tiempo de respuesta debe ser inferior a 400 ms (fue " + duracionMs + " ms)");
    }

    @Test
    @DisplayName("HU-21 Criterio 2: Detecta suscripción vencida, la actualiza a EXPIRADA y activa alerta")
    void debeDetectarSuscripcionVencidaActualizarEstadoAExpiradaYActivarAlerta() throws Exception {
        String[] authData = registrarYLoguearCliente(
                "Sena Regional Cauca",
                "899999034-1",
                "cauca@sena.edu.co",
                "Sena2026*Clave"
        );
        String tokenJwt = authData[0];
        UUID idInstitucion = UUID.fromString(authData[1]);

        // Simular en base de datos que la suscripción expiró ayer
        SuscripcionInstitucional suscripcion = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .orElseThrow();
        suscripcion.setFechaExpiracion(Instant.now().minusSeconds(86400));
        repositorioSuscripcionInstitucional.save(suscripcion);

        // Consultar nuevamente el estado
        mockMvc.perform(get("/api/v1/institutional/suscripciones/mi-estado")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoSuscripcion", is("EXPIRADA")))
                .andExpect(jsonPath("$.vigente", is(false)))
                .andExpect(jsonPath("$.diasRestantes", is(0)))
                .andExpect(jsonPath("$.alertaVencimiento", is(true)));

        // Verificar que en base de datos el estado quedó persistido como EXPIRADA
        SuscripcionInstitucional suscripcionPersistida = repositorioSuscripcionInstitucional
                .findById(suscripcion.getIdSuscripcion())
                .orElseThrow();
        assertTrue("EXPIRADA".equalsIgnoreCase(suscripcionPersistida.getEstadoSuscripcion()));
    }

    @Test
    @DisplayName("HU-21 Criterio 1: Rechaza consulta sin cabecera de autenticación")
    void debeDenegarConsultaSinTokenDeAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/institutional/suscripciones/mi-estado"))
                .andExpect(status().isUnauthorized());
    }
}
