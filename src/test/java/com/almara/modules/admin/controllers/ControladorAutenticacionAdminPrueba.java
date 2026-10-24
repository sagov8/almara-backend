package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.models.Administrador;
import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioAdministrador;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorAutenticacionAdminPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioAdministrador repositorioAdministrador;

    @Autowired
    private RepositorioAuditoriaAdmin repositorioAuditoriaAdmin;

    @BeforeEach
    void setUp() {
        // Desbloquear al admin por defecto antes de cada prueba para aislar escenarios
        repositorioAdministrador.findByCorreoIgnoreCase("admin@almara.com").ifPresent(admin -> {
            admin.restablecerIntentosFallidos();
            admin.setActivo(true);
            repositorioAdministrador.save(admin);
        });
    }

    @Test
    @DisplayName("HU-11: Inicio de sesión exitoso con credenciales válidas emitiendo token JWT con ROLE_ADMIN y registrando auditoría")
    void debeIniciarSesionExitosamenteConCredencialesValidas() throws Exception {
        LoginAdminSolicitud solicitud = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");

        MvcResult resultado = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.correo").value("admin@almara.com"))
                .andExpect(jsonPath("$.nombreCompleto").value("Administrador Principal Almara"))
                .andExpect(jsonPath("$.expiraEnSegundos").isNumber())
                .andReturn();

        LoginAdminRespuesta respuesta = objectMapper.readValue(
                resultado.getResponse().getContentAsString(), LoginAdminRespuesta.class);
        assertThat(respuesta.getToken()).isNotBlank();

        // Validar registro en bitácora de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "LOGIN_ADMIN_EXITOSO".equals(a.getAccion())
                && "admin@almara.com".equals(a.getActorResponsable()));
    }

    @Test
    @DisplayName("HU-11: Rechazo de autenticación ante contraseña incorrecta con mensaje genérico y registro de intento fallido")
    void debeRechazarLoginConContrasenaInvalidaYRegistrarAuditoria() throws Exception {
        LoginAdminSolicitud solicitud = new LoginAdminSolicitud("admin@almara.com", "ClaveIncorrecta999*");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.mensaje").value("Credenciales administrativas incorrectas o cuenta no autorizada"));

        // Validar que se incrementó el intento fallido
        Administrador admin = repositorioAdministrador.findByCorreoIgnoreCase("admin@almara.com").orElseThrow();
        assertThat(admin.getIntentosFallidos()).isEqualTo(1);

        // Validar registro en bitácora de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "LOGIN_ADMIN_FALLIDO".equals(a.getAccion()));
    }

    @Test
    @DisplayName("HU-11: Rechazo de autenticación ante usuario administrador inexistente con mensaje neutral")
    void debeRechazarLoginParaUsuarioInexistente() throws Exception {
        LoginAdminSolicitud solicitud = new LoginAdminSolicitud("fantasma@almara.com", "Clave1234*");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    @DisplayName("HU-11: Bloqueo automático de cuenta tras 5 intentos fallidos consecutivos (Criterio de Aceptación 4)")
    void debeBloquearCuentaTrasCincoIntentosFallidosConsecutivos() throws Exception {
        LoginAdminSolicitud solicitudErronea = new LoginAdminSolicitud("admin@almara.com", "ClaveMala123*");

        // Intentos 1 al 4: 401 Unauthorized
        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/api/v1/admin/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(solicitudErronea)))
                    .andExpect(status().isUnauthorized());
        }

        // Intento 5: Supera el umbral y queda bloqueado (423 Locked)
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudErronea)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("CUENTA_BLOQUEADA_TEMPORALMENTE"))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("Ha superado el límite de intentos fallidos")));

        // Intento 6 (Incluso con contraseña correcta, sigue bloqueado)
        LoginAdminSolicitud solicitudCorrecta = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");
        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudCorrecta)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.error").value("CUENTA_BLOQUEADA_TEMPORALMENTE"));

        // Validar registro de bloqueo en auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "ADMIN_BLOQUEADO_TEMPORALMENTE".equals(a.getAccion()));
    }

    @Test
    @DisplayName("HU-11: Validación de obligatoriedad de campos de entrada (usuario y contraseña)")
    void debeValidarCamposObligatorios() throws Exception {
        LoginAdminSolicitud solicitudVacia = new LoginAdminSolicitud("", "");

        mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudVacia)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION_FALLIDA"));
    }

    @Test
    @DisplayName("HU-11: Consulta de sesión activa con token JWT válido")
    void debeConsultarSesionActualConTokenValido() throws Exception {
        LoginAdminSolicitud login = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        LoginAdminRespuesta loginResp = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginAdminRespuesta.class);

        mockMvc.perform(get("/api/v1/admin/auth/sesion-actual")
                        .header("Authorization", "Bearer " + loginResp.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autenticado").value(true))
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.correo").value("admin@almara.com"))
                .andExpect(jsonPath("$.nombreCompleto").value("Administrador Principal Almara"));
    }

    @Test
    @DisplayName("HU-11 y HU-18: Cierre de sesión administrativo con revocación inmediata de token en blacklist")
    void debeCerrarSesionYRevocarToken() throws Exception {
        LoginAdminSolicitud login = new LoginAdminSolicitud("admin@almara.com", "Admin1234*");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        LoginAdminRespuesta loginResp = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginAdminRespuesta.class);

        // 1. Cierre de sesión exitoso
        mockMvc.perform(post("/api/v1/admin/auth/logout")
                        .header("Authorization", "Bearer " + loginResp.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitoso").value(true));

        // 2. Comprobar que el token ya no sirve (401 Unauthorized por estar revocado)
        mockMvc.perform(get("/api/v1/admin/auth/sesion-actual")
                        .header("Authorization", "Bearer " + loginResp.getToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESION_ADMIN_REVOCADA"));

        // Validar registro en bitácora de auditoría
        List<AuditoriaAdmin> auditorias = repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
        assertThat(auditorias).anyMatch(a -> "LOGOUT_ADMIN".equals(a.getAccion()));
    }
}
