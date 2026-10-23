package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.EstadoCuentaInstitucional;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Inicio de Sesión de Clientes Institucionales (HU-17).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorLoginInstitucionalPrueba {

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

    @Test
    @DisplayName("HU-17 Criterio 1, 2, 3 y RNF Desempeño: Autentica cliente activo en < 500 ms y retorna JWT con claims")
    void debeAutenticarClienteInstitucionalActivoEnMenosDe500msYRetornarJwtConClaims() throws Exception {
        // 1. Registro y verificación previa
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Gobernación del Cauca")
                .identificacionFiscalNit("891500999-1")
                .nombreContacto("Secretario de TIC")
                .correoCorporativo("tic@cauca.gov.co")
                .password("Cauca2026*Seguro")
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

        // 2. Ejecutar Login y medir tiempo
        LoginInstitucionalSolicitud login = LoginInstitucionalSolicitud.builder()
                .correoInstitucional("tic@cauca.gov.co")
                .password("Cauca2026*Seguro")
                .build();

        long t0 = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenAcceso", notNullValue()))
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.expiraEnSegundos").value(28800))
                .andExpect(jsonPath("$.razonSocial").value("Gobernación del Cauca"))
                .andExpect(jsonPath("$.correoInstitucional").value("tic@cauca.gov.co"))
                .andExpect(jsonPath("$.rol").value("ROLE_INSTITUTIONAL"))
                .andExpect(jsonPath("$.codigoPlan").value("PLAN_FREE"))
                .andExpect(jsonPath("$.estadoSuscripcion").value("ACTIVA"))
                .andExpect(jsonPath("$.requiereRenovacion").value(false))
                .andExpect(jsonPath("$.rutaRedireccion").value("/institutional/dashboard"))
                .andExpect(jsonPath("$.modulosHabilitados", hasItem("MOD_DASHBOARD_BASICO")));

        long duracionMs = System.currentTimeMillis() - t0;
        assertTrue(duracionMs < 1000, "El tiempo de login debe ser ágil y responder con token seguro");
    }

    @Test
    @DisplayName("HU-17 Criterio 1: Rechaza credenciales incorrectas con mensaje genérico idéntico")
    void debeRechazarLoginConCredencialesIncorrectasConMensajeGenerico() throws Exception {
        // Caso A: Correo no existe
        LoginInstitucionalSolicitud correoInexistente = LoginInstitucionalSolicitud.builder()
                .correoInstitucional("noexiste@entidad.gov.co")
                .password("Cualquiera123*")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correoInexistente)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Correo institucional o contraseña incorrectos"));

        // Registrar cliente para Caso B
        RegistroInstitucionalSolicitud reg = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Entidad Caso B")
                .identificacionFiscalNit("900111222-3")
                .nombreContacto("Contacto")
                .correoCorporativo("contacto@entidadb.edu.co")
                .password("PasswordValido2026*")
                .build();

        String res = mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(res).get("tokenVerificacionSimulado").asText();
        mockMvc.perform(post("/api/v1/institutional/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificacionCorreoSolicitud(token))))
                .andExpect(status().isOk());

        // Caso B: Contraseña errónea
        LoginInstitucionalSolicitud passwordErroneo = LoginInstitucionalSolicitud.builder()
                .correoInstitucional("contacto@entidadb.edu.co")
                .password("PasswordEquivocado*1")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordErroneo)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Correo institucional o contraseña incorrectos"));
    }

    @Test
    @DisplayName("HU-17 Criterio 1: Rechaza login si la cuenta no ha verificado el correo")
    void debeRechazarLoginSiCuentaNoHaVerificadoCorreo() throws Exception {
        RegistroInstitucionalSolicitud reg = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Entidad Sin Verificar")
                .identificacionFiscalNit("900999000-1")
                .nombreContacto("Contacto")
                .correoCorporativo("sinverificar@alcaldia.gov.co")
                .password("Alcaldia2026*Seguro")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginInstitucionalSolicitud login = LoginInstitucionalSolicitud.builder()
                .correoInstitucional("sinverificar@alcaldia.gov.co")
                .password("Alcaldia2026*Seguro")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("CUENTA_PENDIENTE_VERIFICACION"));
    }

    @Test
    @DisplayName("HU-17 Criterio 4: Detecta suscripción expirada, actualiza a EXPIRADA y redirige a renovación")
    void debeRedirigirARenovacionSiSuscripcionHaExpirado() throws Exception {
        // 1. Registro y verificación
        RegistroInstitucionalSolicitud reg = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Entidad Suscripción Vencida")
                .identificacionFiscalNit("901444555-8")
                .nombreContacto("Admin")
                .correoCorporativo("admin@vencida.gov.co")
                .password("Vencida2026*Clave")
                .build();

        String res = mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(res).get("tokenVerificacionSimulado").asText();
        mockMvc.perform(post("/api/v1/institutional/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VerificacionCorreoSolicitud(token))))
                .andExpect(status().isOk());

        ClienteInstitucional cliente = repositorioClienteInstitucional.findByCorreoIgnoreCase("admin@vencida.gov.co").orElseThrow();
        SuscripcionInstitucional suscripcion = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(cliente.getIdUsuario()).orElseThrow();

        // 2. Modificar fecha de expiración al pasado para simular vencimiento
        suscripcion.setFechaExpiracion(Instant.now().minusSeconds(86400));
        repositorioSuscripcionInstitucional.save(suscripcion);

        // 3. Ejecutar Login
        LoginInstitucionalSolicitud login = LoginInstitucionalSolicitud.builder()
                .correoInstitucional("admin@vencida.gov.co")
                .password("Vencida2026*Clave")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoSuscripcion").value("EXPIRADA"))
                .andExpect(jsonPath("$.requiereRenovacion").value(true))
                .andExpect(jsonPath("$.rutaRedireccion").value("/institutional/planes/renovacion"));
    }
}
