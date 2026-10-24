package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioCarritoCompra;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Checkout y Generación de Órdenes de Suscripción (HU-22).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorCheckoutSuscripcionPrueba {

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

    @BeforeEach
    @org.junit.jupiter.api.AfterEach
    void setUp() {
        repositorioAuditoriaInstitucional.deleteAll();
        repositorioOrdenPedido.deleteAll();
        repositorioCarritoCompra.deleteAll();
        repositorioSuscripcionInstitucional.deleteAll();
        repositorioClienteInstitucional.deleteAll();
    }

    private String registrarYLoguearCliente(String nit, String correo) throws Exception {
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial("Universidad del Cauca")
                .identificacionFiscalNit(nit)
                .nombreContacto("Rectoría Institucional")
                .correoCorporativo(correo)
                .password("ClaveSegura2026*")
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
                .password("ClaveSegura2026*")
                .build();

        String respLogin = mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respLogin).get("tokenAcceso").asText();
    }

    @Test
    @DisplayName("Debe adquirir plan y generar orden de suscripción con línea de detalle en menos de 800ms")
    void debeAdquirirPlanYGenerarOrdenDeSuscripcionConLineaDetalleEnMenosDe800ms() throws Exception {
        String token = registrarYLoguearCliente("891500319-1", "contacto@unicauca.edu.co");

        AdquirirPlanSolicitud solicitud = AdquirirPlanSolicitud.builder()
                .codigoPlan("PLAN_STANDARD")
                .metodoPagoPreferido("STRIPE_CARD")
                .build();

        long tiempoInicio = System.currentTimeMillis();

        mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrden", notNullValue()))
                .andExpect(jsonPath("$.numeroOrden", startsWith("ORD-2026-")))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_STANDARD")))
                .andExpect(jsonPath("$.nombrePlan", is("Plan Institucional Estándar")))
                .andExpect(jsonPath("$.duracionDias", is(30)))
                .andExpect(jsonPath("$.estadoOrden", is("PENDIENTE_PAGO")))
                .andExpect(jsonPath("$.montoTotal", is(149000.00)))
                .andExpect(jsonPath("$.lineasDetalle", hasSize(1)))
                .andExpect(jsonPath("$.lineasDetalle[0].cantidad", is(1)))
                .andExpect(jsonPath("$.lineasDetalle[0].precioAplicado", is(149000.00)))
                .andExpect(jsonPath("$.lineasDetalle[0].subtotal", is(149000.00)))
                .andExpect(jsonPath("$.siguientePaso", is("PROCESAR_PAGO")));

        long duracionMs = System.currentTimeMillis() - tiempoInicio;
        assertTrue(duracionMs < 800, "El tiempo de procesamiento del checkout debe ser menor a 800ms (demoró " + duracionMs + "ms)");
    }

    @Test
    @DisplayName("Debe garantizar idempotencia sin duplicar orden pendiente para el mismo plan")
    void debeGarantizarIdempotenciaSinDuplicarOrdenPendienteParaElMismoPlan() throws Exception {
        String token = registrarYLoguearCliente("891500319-2", "alcaldia@popayan.gov.co");

        AdquirirPlanSolicitud solicitud = AdquirirPlanSolicitud.builder()
                .codigoPlan("PLAN_STANDARD")
                .build();

        String resp1 = mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json1 = objectMapper.readTree(resp1);
        String idOrden1 = json1.get("idOrden").asText();
        String numeroOrden1 = json1.get("numeroOrden").asText();

        // Segunda llamada inmediata (simulación de doble clic en UI)
        String resp2 = mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json2 = objectMapper.readTree(resp2);
        String idOrden2 = json2.get("idOrden").asText();
        String numeroOrden2 = json2.get("numeroOrden").asText();

        assertEquals(idOrden1, idOrden2, "Ambas invocaciones deben reutilizar el mismo idOrden");
        assertEquals(numeroOrden1, numeroOrden2, "Ambas invocaciones deben reutilizar el mismo numeroOrden");
        assertEquals(1, repositorioOrdenPedido.count(), "No deben crearse órdenes duplicadas en base de datos");
    }

    @Test
    @DisplayName("Debe rechazar creación de orden con plan inexistente con error 404")
    void debeRechazarCreacionDeOrdenConPlanInexistente() throws Exception {
        String token = registrarYLoguearCliente("891500319-3", "innovacion@popayan.gov.co");

        AdquirirPlanSolicitud solicitud = AdquirirPlanSolicitud.builder()
                .codigoPlan("PLAN_INEXISTENTE")
                .build();

        mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("PLAN_NO_ENCONTRADO")));
    }
}
