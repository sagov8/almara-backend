package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.adapters.notification.AdaptadorNotificacionSimulada;
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

import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Registro y Verificación de Clientes Institucionales (HU-16).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorRegistroInstitucionalPrueba {

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

    @Autowired
    private AdaptadorNotificacionSimulada adaptadorNotificacionSimulada;

    @BeforeEach
    void setUp() {
        repositorioAuditoriaInstitucional.deleteAll();
        repositorioSuscripcionInstitucional.deleteAll();
        repositorioClienteInstitucional.deleteAll();
        adaptadorNotificacionSimulada.limpiar();
    }

    @Test
    @DisplayName("HU-16 Criterio 1, 3, 4: Registra cliente institucional con datos válidos, genera token y envía correo")
    void debeRegistrarClienteInstitucionalExitosamenteYEnviarCorreoVerificacion() throws Exception {
        RegistroInstitucionalSolicitud solicitud = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Alcaldía Municipal de Popayán")
                .identificacionFiscalNit("891500761-4")
                .nombreContacto("Secretario General")
                .correoCorporativo("contacto@popayan.gov.co")
                .password("Popayan2026*Seguro")
                .telefonoContacto("3101234567")
                .sectorActividad("GOBIERNO")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInstitucion", notNullValue()))
                .andExpect(jsonPath("$.razonSocial").value("Alcaldía Municipal de Popayán"))
                .andExpect(jsonPath("$.correoCorporativo").value("contacto@popayan.gov.co"))
                .andExpect(jsonPath("$.estadoCuenta").value(EstadoCuentaInstitucional.PENDIENTE_APROBACION.name()))
                .andExpect(jsonPath("$.correoVerificado").value(false))
                .andExpect(jsonPath("$.tokenVerificacionSimulado", notNullValue()));

        // Verificar persistencia en base de datos
        Optional<ClienteInstitucional> clienteOpt = repositorioClienteInstitucional.findByCorreoIgnoreCase("contacto@popayan.gov.co");
        assertTrue(clienteOpt.isPresent(), "El cliente institucional debe estar persistido en BD");
        ClienteInstitucional cliente = clienteOpt.get();
        assertEquals("891500761-4", cliente.getIdentificacionFiscalNit());
        assertFalse(cliente.isCorreoVerificado());
        assertNotNull(cliente.getTokenVerificacion());

        // Verificar que el correo no quedó guardado en texto plano
        assertNotEquals("Popayan2026*Seguro", cliente.getPasswordHash());

        // Verificar despacho por el puerto ServicioNotificacionPort
        Optional<AdaptadorNotificacionSimulada.NotificacionEmitida> notifOpt =
                adaptadorNotificacionSimulada.obtenerUltimaNotificacionPorDestinatario("contacto@popayan.gov.co");
        assertTrue(notifOpt.isPresent(), "Debe haberse emitido una notificación al correo corporativo");
        assertEquals(cliente.getTokenVerificacion(), notifOpt.get().tokenVerificacion());
    }

    @Test
    @DisplayName("HU-16 Criterio 1: Rechaza registro con dominio de correo público gratuito")
    void debeRechazarRegistroConDominioDeCorreoNoCorporativo() throws Exception {
        RegistroInstitucionalSolicitud solicitud = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Entidad Demo Gmail")
                .identificacionFiscalNit("900123456-1")
                .nombreContacto("Representante")
                .correoCorporativo("usuario@gmail.com")
                .password("ClaveSegura2026#")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("CORREO_NO_CORPORATIVO"))
                .andExpect(jsonPath("$.mensaje", containsString("No se permiten correos de dominios públicos gratuitos")));
    }

    @Test
    @DisplayName("HU-16 Criterio 2: Rechaza contraseña débil sin caracteres especiales o mayúsculas")
    void debeRechazarPasswordDebilSinCaracteresEspecialesOMayusculas() throws Exception {
        RegistroInstitucionalSolicitud solicitud = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Universidad del Cauca")
                .identificacionFiscalNit("891500319-2")
                .nombreContacto("Rectoría")
                .correoCorporativo("rectoria@unicauca.edu.co")
                .password("password123") // Débil: sin mayúscula ni símbolo
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PASSWORD_NO_SEGURO"));
    }

    @Test
    @DisplayName("HU-16 Criterio 1: Rechaza NIT o correo duplicado retornando 409 CONFLICT")
    void debeRechazarNitOCorreoDuplicado() throws Exception {
        RegistroInstitucionalSolicitud solicitud1 = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Cámara de Comercio del Cauca")
                .identificacionFiscalNit("891500123-5")
                .nombreContacto("Director")
                .correoCorporativo("presidencia@cccauca.org.co")
                .password("Cccauca2026@Seguro")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud1)))
                .andExpect(status().isCreated());

        // Intento 2 con el mismo correo
        RegistroInstitucionalSolicitud solicitudCorreoDuplicado = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Otra Entidad")
                .identificacionFiscalNit("901999888-7")
                .nombreContacto("Otro")
                .correoCorporativo("presidencia@cccauca.org.co")
                .password("Cccauca2026@Seguro")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudCorreoDuplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTIDAD_YA_REGISTRADA"));

        // Intento 3 con el mismo NIT
        RegistroInstitucionalSolicitud solicitudNitDuplicado = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Tercera Entidad")
                .identificacionFiscalNit("891500123-5")
                .nombreContacto("Tercero")
                .correoCorporativo("diferente@cccauca.org.co")
                .password("Cccauca2026@Seguro")
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitudNitDuplicado)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("ENTIDAD_YA_REGISTRADA"));
    }

    @Test
    @DisplayName("HU-16 Activación autónoma: Verifica token de correo, pasa a ACTIVA y crea suscripción PLAN_FREE")
    void debeVerificarCorreoActivarCuentaYAsignarPlanFreePorDefecto() throws Exception {
        // 1. Registro inicial
        RegistroInstitucionalSolicitud solicitud = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Compañía Energética de Occidente")
                .identificacionFiscalNit("900350123-9")
                .nombreContacto("Gerente de Operaciones")
                .correoCorporativo("atencion@energeticadeoccidente.com")
                .password("Energia2026*Valida")
                .build();

        String respuestaJson = mockMvc.perform(post("/api/v1/institutional/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(respuestaJson).get("tokenVerificacionSimulado").asText();
        assertNotNull(token);

        // 2. Verificación de correo con el token
        VerificacionCorreoSolicitud verificacion = VerificacionCorreoSolicitud.builder()
                .tokenVerificacion(token)
                .build();

        mockMvc.perform(post("/api/v1/institutional/auth/verificar-correo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verificacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoCuenta").value(EstadoCuentaInstitucional.ACTIVA.name()))
                .andExpect(jsonPath("$.correoVerificado").value(true))
                .andExpect(jsonPath("$.codigoPlan").value("PLAN_FREE"))
                .andExpect(jsonPath("$.idSuscripcion", notNullValue()))
                .andExpect(jsonPath("$.fechaExpiracionSuscripcion", notNullValue()));

        // Verificar en BD que el cliente está ACTIVO y tiene suscripción activa
        ClienteInstitucional cliente = repositorioClienteInstitucional.findByCorreoIgnoreCase("atencion@energeticadeoccidente.com").orElseThrow();
        assertEquals(EstadoCuentaInstitucional.ACTIVA.name(), cliente.getEstadoCuenta());
        assertTrue(cliente.isCorreoVerificado());
        assertNull(cliente.getTokenVerificacion());

        Optional<SuscripcionInstitucional> suscripcionOpt = repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(cliente.getIdUsuario());
        assertTrue(suscripcionOpt.isPresent());
        SuscripcionInstitucional suscripcion = suscripcionOpt.get();
        assertEquals("ACTIVA", suscripcion.getEstadoSuscripcion());
        assertTrue(suscripcion.estaVigente());
    }
}
