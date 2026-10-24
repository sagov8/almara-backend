package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.LoginInstitucionalSolicitud;
import com.almara.modules.institutional.dto.ProcesarPagoSolicitud;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas automatizadas de integración para el Control de Acceso por Módulos y Plan Activo (HU-20).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControladorPermisosPlanPrueba {

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
                .razonSocial("Colegio Mayor del Cauca")
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
    @DisplayName("Debe permitir acceso a módulo básico con PLAN_FREE y restringir módulos premium con 403")
    void debePermitirAccesoAModuloBasicoConPlanFreeYRestringirModulosPremiumCon403() throws Exception {
        String token = registrarYLoguearCliente("891500500-1", "mayor@colmayor.edu.co");

        // 1. Módulo básico permitido en PLAN_FREE
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_DASHBOARD_BASICO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesoPermitido", is(true)))
                .andExpect(jsonPath("$.codigoModulo", is("MOD_DASHBOARD_BASICO")))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_FREE")));

        // 2. Módulo de Filtros Territoriales restringido en PLAN_FREE -> 403
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_FILTROS_TERRITORIALES")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("MODULO_RESTRINGIDO_POR_PLAN")))
                .andExpect(jsonPath("$.detallesAdicionales.codigoModulo", is("MOD_FILTROS_TERRITORIALES")))
                .andExpect(jsonPath("$.detallesAdicionales.planActual", is("PLAN_FREE")));

        // 3. Módulo de Exportación restringido en PLAN_FREE -> 403
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_EXPORTACION_REPORTES")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("MODULO_RESTRINGIDO_POR_PLAN")));
    }

    @Test
    @DisplayName("Debe habilitar módulo de filtros territoriales inmediatamente tras adquirir y pagar PLAN_STANDARD")
    void debeHabilitarModuloFiltrosTerritorialesInmediatamenteTrasAdquirirYPagarPlanStandard() throws Exception {
        String token = registrarYLoguearCliente("891500500-2", "alcaldia@colmayor.edu.co");

        // Inicialmente con PLAN_FREE no tiene acceso
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_FILTROS_TERRITORIALES")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        // Adquirir PLAN_STANDARD (Checkout HU-22)
        String respOrden = mockMvc.perform(post("/api/v1/institutional/checkout/orden")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AdquirirPlanSolicitud("PLAN_STANDARD", "STRIPE_CARD"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String idOrden = objectMapper.readTree(respOrden).get("idOrden").asText();

        // Procesar pago aprobado (HU-23)
        ProcesarPagoSolicitud pagoSolicitud = ProcesarPagoSolicitud.builder()
                .idOrden(UUID.fromString(idOrden))
                .metodoPago("TARJETA_CREDITO")
                .tokenMedioPago("tok_visa_4242")
                .build();

        String respPago = mockMvc.perform(post("/api/v1/institutional/pagos/procesar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagoSolicitud)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tokenActualizado = objectMapper.readTree(respPago).get("tokenAccesoActualizado").asText();

        // Ahora debe tener acceso inmediato a MOD_FILTROS_TERRITORIALES
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_FILTROS_TERRITORIALES")
                        .header("Authorization", "Bearer " + tokenActualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accesoPermitido", is(true)))
                .andExpect(jsonPath("$.codigoModulo", is("MOD_FILTROS_TERRITORIALES")))
                .andExpect(jsonPath("$.codigoPlan", is("PLAN_STANDARD")));
    }

    @Test
    @DisplayName("Debe revocar inmediatamente el acceso a todos los módulos cuando la suscripción expira y registrar auditoría")
    void debeRevocarInmediatamenteElAccesoATodosLosModulosCuandoLaSuscripcionExpiraYRegistrarAuditoria() throws Exception {
        String token = registrarYLoguearCliente("891500500-3", "auditoria@colmayor.edu.co");

        // Vencer artificialmente la suscripción en base de datos
        List<SuscripcionInstitucional> suscripciones = repositorioSuscripcionInstitucional.findAll();
        for (SuscripcionInstitucional s : suscripciones) {
            s.setFechaExpiracion(Instant.now().minusSeconds(120));
            s.setEstadoSuscripcion("ACTIVA");
            repositorioSuscripcionInstitucional.save(s);
        }

        // Acceso a cualquier módulo debe ser revocado con 403 SUSCRIPCION_EXPIRADA
        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_DASHBOARD_BASICO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("SUSCRIPCION_EXPIRADA")));

        mockMvc.perform(get("/api/v1/institutional/permisos/validar/MOD_FILTROS_TERRITORIALES")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("SUSCRIPCION_EXPIRADA")));

        // Verificar que se registró auditoría del cambio de estado a EXPIRADA
        List<AuditoriaInstitucional> auditorias = repositorioAuditoriaInstitucional.findAll();
        assertTrue(auditorias.stream().anyMatch(a -> "ESTADO_SUSCRIPCION_ACTUALIZADO".equals(a.getTipoEvento())),
                "Debe registrarse en auditoría el cambio de estado a EXPIRADA");
    }
}
