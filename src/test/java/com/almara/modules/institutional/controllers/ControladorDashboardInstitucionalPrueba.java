package com.almara.modules.institutional.controllers;

import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.ProcesarPagoSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioCarritoCompra;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioPago;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Dashboard Analítico Institucional y Filtrado Territorial (HU-24, RF-34).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorDashboardInstitucionalPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositorioClienteInstitucional repositorioClienteInstitucional;

    @Autowired
    private RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;

    @Autowired
    private RepositorioOrdenPedido repositorioOrdenPedido;

    @Autowired
    private RepositorioCarritoCompra repositorioCarritoCompra;

    @Autowired
    private RepositorioPago repositorioPago;

    @Autowired
    private RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @Autowired
    private ServicioAgregacionMapa servicioAgregacionMapa;

    @BeforeEach
    @AfterEach
    void limpiarBaseDatos() {
        repositorioAuditoriaInstitucional.deleteAll();
        repositorioPago.deleteAll();
        repositorioOrdenPedido.deleteAll();
        repositorioCarritoCompra.deleteAll();
        repositorioSuscripcionInstitucional.deleteAll();
        repositorioClienteInstitucional.deleteAll();
        repositorioReporteEmocion.limpiar();
        servicioAgregacionMapa.sembrarReportesIniciales();
    }

    private String registrarYLoguearCliente(String nit, String correo, String razonSocial) throws Exception {
        RegistroInstitucionalSolicitud registro = RegistroInstitucionalSolicitud.builder()
                .razonSocial(razonSocial)
                .identificacionFiscalNit(nit)
                .nombreContacto("Director Planeación")
                .correoCorporativo(correo)
                .password("Almara2026*Secure")
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
                .password("Almara2026*Secure")
                .build();

        String respLogin = mockMvc.perform(post("/api/v1/institutional/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respLogin).get("tokenAcceso").asText();
    }

    private String adquirirPlanYPagar(String tokenActual, String codigoPlan) throws Exception {
        // 1. Generar orden de compra (HU-22)
        String respOrden = mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + tokenActual)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdquirirPlanSolicitud(codigoPlan, "STRIPE_CARD"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String idOrden = objectMapper.readTree(respOrden).get("idOrden").asText();

        // 2. Procesar pago exitoso (HU-23)
        ProcesarPagoSolicitud pagoSolicitud = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_visa_4242")
                .build();

        String respPago = mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + tokenActual)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoSolicitud)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respPago).get("tokenAccesoActualizado").asText();
    }

    @Test
    @DisplayName("Debe consultar dashboard resumen con PLAN_FREE en menos de 2 segundos respetando k-anonimato")
    void debeConsultarDashboardResumenConPlanFreeEnMenosDe2SegundosRespetandoKAnonimato() throws Exception {
        String token = registrarYLoguearCliente("891500701-1", "planeacion@popayan.gov.co", "Alcaldía de Popayán");

        mockMvc.perform(get("/api/v1/institutional/dashboard/resumen")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPlanActivo", is("PLAN_FREE")))
                .andExpect(jsonPath("$.razonSocial", is("Alcaldía de Popayán")))
                .andExpect(jsonPath("$.filtroTerritorialHabilitado", is(false)))
                .andExpect(jsonPath("$.exportacionHabilitada", is(false)))
                .andExpect(jsonPath("$.tiempoCalculoMs", lessThan(2000)))
                .andExpect(jsonPath("$.totalZonasMonitoreadas", greaterThanOrEqualTo(15)))
                .andExpect(jsonPath("$.zonasConDatosSuficientes", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.zonasEnReservaPrivacidad", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.distribucionGlobalCiudad", hasSize(5)))
                // Validar k-anonimato: ZONA-PANTEON-PROCERES tiene < 5 reportes
                .andExpect(jsonPath("$.resumenZonas[?(@.zonaManualId == 'ZONA-PANTEON-PROCERES')].cumpleUmbral", contains(false)))
                .andExpect(jsonPath("$.resumenZonas[?(@.zonaManualId == 'ZONA-PANTEON-PROCERES')].emocionPredominante", contains((String) null)))
                .andExpect(jsonPath("$.resumenZonas[?(@.zonaManualId == 'ZONA-PANTEON-PROCERES')].icono", contains("Lock")))
                // Validar zona con suficientes reportes (k >= 5)
                .andExpect(jsonPath("$.resumenZonas[?(@.zonaManualId == 'ZONA-PARQUE-CALDAS')].cumpleUmbral", contains(true)))
                .andExpect(jsonPath("$.resumenZonas[?(@.zonaManualId == 'ZONA-PARQUE-CALDAS')].emocionPredominante", notNullValue()));
    }

    @Test
    @DisplayName("Debe denegar filtrado territorial a cliente con PLAN_FREE retornando 403 Forbidden")
    void debeDenegarFiltradoTerritorialAClienteConPlanFreeRetornando403Forbidden() throws Exception {
        String token = registrarYLoguearCliente("891500701-2", "salud@popayan.gov.co", "Secretaría de Salud Popayán");

        mockMvc.perform(get("/api/v1/institutional/dashboard/filtrar-zonas")
                        .param("zonaManualId", "ZONA-PARQUE-CALDAS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("MODULO_RESTRINGIDO_POR_PLAN")))
                .andExpect(jsonPath("$.detallesAdicionales.codigoModulo", is("MOD_FILTROS_TERRITORIALES")))
                .andExpect(jsonPath("$.detallesAdicionales.planActual", is("PLAN_FREE")));
    }

    @Test
    @DisplayName("Debe permitir filtrar clima emocional por zona territorial con PLAN_STANDARD")
    void debePermitirFiltrarClimaEmocionalPorZonaTerritorialConPlanStandard() throws Exception {
        String tokenFree = registrarYLoguearCliente("891500701-3", "movilidad@popayan.gov.co", "Secretaría de Movilidad");

        // Subir a PLAN_STANDARD
        String tokenStandard = adquirirPlanYPagar(tokenFree, "PLAN_STANDARD");

        mockMvc.perform(get("/api/v1/institutional/dashboard/filtrar-zonas")
                        .param("zonaManualId", "ZONA-PARQUE-CALDAS")
                        .header("Authorization", "Bearer " + tokenStandard))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalZonasCoincidentes", is(1)))
                .andExpect(jsonPath("$.zonasFiltradas[0].zonaManualId", is("ZONA-PARQUE-CALDAS")))
                .andExpect(jsonPath("$.zonasFiltradas[0].nombreZona", containsString("Parque Caldas")))
                .andExpect(jsonPath("$.zonasFiltradas[0].cumpleUmbral", is(true)))
                .andExpect(jsonPath("$.zonasFiltradas[0].emocionPredominante", notNullValue()))
                .andExpect(jsonPath("$.tiempoCalculoMs", lessThan(2000)));
    }

    @Test
    @DisplayName("Debe permitir exportar reporte analítico solo con PLAN_ENTERPRISE")
    void debePermitirExportarReporteAnaliticoSoloConPlanEnterprise() throws Exception {
        String tokenFree = registrarYLoguearCliente("891500701-4", "despacho@popayan.gov.co", "Despacho del Alcalde");

        // Con PLAN_STANDARD, exportar debe retornar 403 FORBIDDEN
        String tokenStandard = adquirirPlanYPagar(tokenFree, "PLAN_STANDARD");

        mockMvc.perform(get("/api/v1/institutional/dashboard/exportar")
                        .header("Authorization", "Bearer " + tokenStandard))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("MODULO_RESTRINGIDO_POR_PLAN")))
                .andExpect(jsonPath("$.detallesAdicionales.codigoModulo", is("MOD_EXPORTACION_REPORTES")));

        // Subir a PLAN_ENTERPRISE
        String tokenEnterprise = adquirirPlanYPagar(tokenStandard, "PLAN_ENTERPRISE");

        mockMvc.perform(get("/api/v1/institutional/dashboard/exportar")
                        .header("Authorization", "Bearer " + tokenEnterprise))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planSuscripcion", is("PLAN_ENTERPRISE")))
                .andExpect(jsonPath("$.formato", is("JSON")))
                .andExpect(jsonPath("$.totalZonasExportadas", greaterThan(0)))
                .andExpect(jsonPath("$.declaracionPrivacidad", containsString("k-anonimato")));
    }
}
