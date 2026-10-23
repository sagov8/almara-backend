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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Cierre de Sesión y Revocación de Token (HU-18).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorLogoutInstitucionalPrueba {

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
                .nombreContacto("Director de Seguridad")
                .correoCorporativo(correo)
                .password(password)
                .sectorActividad("MINERIA_ENERGIA")
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
    @DisplayName("HU-18 Criterio 1 y 2: Cierra sesión y revoca token impidiendo reutilización posterior")
    void debeCerrarSesionYRevocarTokenDeFormaInmediata() throws Exception {
        String tokenJwt = registrarYLoguearCliente(
                "Ecopetrol Operaciones",
                "899999068-1",
                "seguridad@ecopetrol.com.co",
                "EcoPetrol2026*Seguro"
        );

        // 1. Verificar sesión activa inicialmente
        mockMvc.perform(get("/api/v1/institutional/auth/sesion-actual")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correoInstitucional", is("seguridad@ecopetrol.com.co")))
                .andExpect(jsonPath("$.razonSocial", is("Ecopetrol Operaciones")));

        // 2. Ejecutar Cierre de Sesión (POST /api/v1/institutional/auth/logout)
        mockMvc.perform(post("/api/v1/institutional/auth/logout")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenRevocado", is(true)))
                .andExpect(jsonPath("$.mensaje", is("Sesión cerrada exitosamente y token de acceso revocado")));

        // 3. Comprobar que cualquier intento posterior con ese mismo token es rechazado con 401 UNAUTHORIZED
        mockMvc.perform(get("/api/v1/institutional/auth/sesion-actual")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("TOKEN_REVOCADO")));

        // 4. Comprobar auditoría generada (RNF-13)
        boolean existeAuditoriaLogout = repositorioAuditoriaInstitucional.findAll().stream()
                .anyMatch(a -> "LOGOUT_INSTITUCIONAL".equals(a.getTipoEvento()));
        assertTrue(existeAuditoriaLogout, "Debe quedar registro de auditoría del evento LOGOUT_INSTITUCIONAL");
    }

    @Test
    @DisplayName("HU-18 Criterio 1: Rechaza cierre de sesión sin cabecera Authorization o con token revocado")
    void debeRechazarCierreDeSesionSinTokenOConTokenYaRevocado() throws Exception {
        String tokenJwt = registrarYLoguearCliente(
                "Celsia Energía",
                "900000123-4",
                "admin@celsia.com",
                "Celsia2026*Clave"
        );

        // Intento de logout sin cabecera
        mockMvc.perform(post("/api/v1/institutional/auth/logout"))
                .andExpect(status().isUnauthorized());

        // Primer logout exitoso
        mockMvc.perform(post("/api/v1/institutional/auth/logout")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk());

        // Segundo intento de logout con el mismo token revocado
        mockMvc.perform(post("/api/v1/institutional/auth/logout")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("TOKEN_REVOCADO")));
    }
}
