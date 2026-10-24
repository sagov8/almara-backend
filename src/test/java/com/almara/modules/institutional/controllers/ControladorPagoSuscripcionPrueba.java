package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.ProcesarPagoSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.Pago;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioCarritoCompra;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioPago;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Procesamiento y Reintento de Pagos de Suscripción (HU-23).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorPagoSuscripcionPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioClienteInstitucional repositorioClienteInstitucional;

    @Autowired
    private RepositorioOrdenPedido repositorioOrdenPedido;

    @Autowired
    private RepositorioCarritoCompra repositorioCarritoCompra;

    @Autowired
    private RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;

    @Autowired
    private RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;

    @Autowired
    private RepositorioPago repositorioPago;

    @BeforeEach
    @AfterEach
    void limpiarBaseDatos() {
        repositorioAuditoriaInstitucional.deleteAll();
        repositorioPago.deleteAll();
        repositorioOrdenPedido.deleteAll();
        repositorioCarritoCompra.deleteAll();
        repositorioSuscripcionInstitucional.deleteAll();
        repositorioClienteInstitucional.deleteAll();
    }

    private String registrarYLoguearCliente(String nit, String correo) throws Exception {
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Comfacauca")
                .identificacionFiscalNit(nit)
                .nombreContacto("Gerencia Financiera")
                .correoCorporativo(correo)
                .password("ClaveSegura2026*")
                .sectorActividad("PRIVADO")
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
                .password("ClaveSegura2026*")
                .build();

        String respLogin = mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respLogin).get("tokenAcceso").asText();
    }

    private String crearOrden(String token, String codigoPlan) throws Exception {
        AdquirirPlanSolicitud solicitud = AdquirirPlanSolicitud.builder()
                .codigoPlan(codigoPlan)
                .metodoPagoPreferido("STRIPE_CARD")
                .build();

        String respOrden = mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respOrden).get("idOrden").asText();
    }

    @Test
    @DisplayName("Debe procesar pago aprobado, activar suscripción, actualizar JWT y emitir notificación")
    void debeProcesarPagoAprobadoActivarSuscripcionActualizarJwtYEmitirNotificacion() throws Exception {
        String token = registrarYLoguearCliente("891500400-1", "pagos@comfacauca.com");
        String idOrden = crearOrden(token, "PLAN_STANDARD");

        ProcesarPagoSolicitud pagoSolicitud = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_visa_4242")
                .build();

        mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoSolicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago", notNullValue()))
                .andExpect(jsonPath("$.idOrden", is(idOrden)))
                .andExpect(jsonPath("$.referenciaExterna", startsWith("PAY-SIM-")))
                .andExpect(jsonPath("$.pagoAprobado", is(true)))
                .andExpect(jsonPath("$.permiteReintento", is(false)))
                .andExpect(jsonPath("$.estadoPago", is("APROBADO")))
                .andExpect(jsonPath("$.estadoOrden", is("PAGADA")))
                .andExpect(jsonPath("$.codigoPlanActivado", is("PLAN_STANDARD")))
                .andExpect(jsonPath("$.estadoSuscripcion", is("ACTIVA")))
                .andExpect(jsonPath("$.nuevaFechaExpiracion", notNullValue()))
                .andExpect(jsonPath("$.notificacionEnviada", is(true)))
                .andExpect(jsonPath("$.tokenAccesoActualizado", notNullValue()))
                .andExpect(jsonPath("$.mensaje", containsString("aprobado con éxito")));

        // Validar que la suscripción en base de datos quedó en ACTIVA
        List<SuscripcionInstitucional> suscripciones = repositorioSuscripcionInstitucional.findAll();
        assertFalse(suscripciones.isEmpty());
        assertEquals("ACTIVA", suscripciones.get(0).getEstadoSuscripcion());

        // Validar auditoría
        List<AuditoriaInstitucional> auditorias = repositorioAuditoriaInstitucional.findAll();
        assertTrue(auditorias.stream().anyMatch(a -> "PAGO_APROBADO_Y_SUSCRIPCION_ACTIVADA".equals(a.getTipoEvento())));
    }

    @Test
    @DisplayName("Debe procesar pago rechazado, permitir reintento y posteriormente aprobarlo")
    void debeProcesarPagoRechazadoPermitirReintentoYPosteriormenteAprobarlo() throws Exception {
        String token = registrarYLoguearCliente("891500400-2", "finanzas@comfacauca.com");
        String idOrden = crearOrden(token, "PLAN_STANDARD");

        // 1. Intento inicial con token de rechazo determinista
        ProcesarPagoSolicitud pagoRechazado = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_rechazar_fondos")
                .build();

        mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoRechazado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagoAprobado", is(false)))
                .andExpect(jsonPath("$.permiteReintento", is(true)))
                .andExpect(jsonPath("$.estadoPago", is("RECHAZADO")))
                .andExpect(jsonPath("$.estadoOrden", is("FALLIDA")))
                .andExpect(jsonPath("$.motivoRechazo", containsString("Fondos insuficientes")));

        // 2. Reintento con tarjeta válida usando el endpoint de reintento
        ProcesarPagoSolicitud pagoReintentoExitoso = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_visa_4242")
                .build();

        mockMvc.perform(post("/api/v1/institutional/pagos/reintentar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoReintentoExitoso)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagoAprobado", is(true)))
                .andExpect(jsonPath("$.permiteReintento", is(false)))
                .andExpect(jsonPath("$.estadoPago", is("APROBADO")))
                .andExpect(jsonPath("$.estadoOrden", is("PAGADA")))
                .andExpect(jsonPath("$.codigoPlanActivado", is("PLAN_STANDARD")));

        // Validar que se registraron exactamente dos transacciones para la misma orden
        List<Pago> pagos = repositorioPago.findByIdOrdenOrderByFechaTransaccionDesc(UUID.fromString(idOrden));
        assertEquals(2, pagos.size(), "Debe haber dos intentos de pago registrados");
        assertEquals("APROBADO", pagos.get(0).getEstadoPago());
        assertEquals("RECHAZADO", pagos.get(1).getEstadoPago());
    }

    @Test
    @DisplayName("Debe rechazar cobro sobre orden ya pagada con error 409 Conflict")
    void debeRechazarCobroSobreOrdenYaPagada() throws Exception {
        String token = registrarYLoguearCliente("891500400-3", "tesoreria@comfacauca.com");
        String idOrden = crearOrden(token, "PLAN_STANDARD");

        ProcesarPagoSolicitud pagoExitoso = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_visa_4242")
                .build();

        // Primer pago: Exitoso
        mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoExitoso)))
                .andExpect(status().isOk());

        // Segundo cobro sobre la misma orden ya pagada
        mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoExitoso)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("ORDEN_YA_PAGADA")));
    }
}
