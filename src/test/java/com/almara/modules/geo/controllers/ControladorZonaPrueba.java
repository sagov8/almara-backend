package com.almara.modules.geo.controllers;

import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.ResolucionZonaSolicitud;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ControladorZonaPrueba {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapeadorObjetos;

    @Test
    @DisplayName("HU-04: Consulta del catálogo de zonas predefinidas para selección manual")
    void obtenerZonasManuales_Exitoso() throws Exception {
        mockMvc.perform(get("/api/v1/geo/zonas-manuales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[*].nombre", hasItems("Chapinero Central", "Usaquén", "Teusaquillo / Parkway")))
                .andExpect(jsonPath("$[*].idCeldaH3", everyItem(hasLength(15))))
                .andExpect(jsonPath("$[*].resolucionH3", everyItem(is(9))));
    }

    @Test
    @DisplayName("HU-04: Resolución de celda H3 por coordenadas GPS temporales")
    void resolverCelda_ConGps_Exitoso() throws Exception {
        ResolucionZonaSolicitud solicitud = ResolucionZonaSolicitud.builder()
                .coordenadasGps(new CoordenadasGps(4.6486, -74.0645))
                .build();

        mockMvc.perform(post("/api/v1/geo/resolver-celda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty())
                .andExpect(jsonPath("$.idCeldaH3", hasLength(15)))
                .andExpect(jsonPath("$.resolucionH3").value(9))
                .andExpect(jsonPath("$.esManual").value(false))
                .andExpect(jsonPath("$.latitudCentroide").isNumber())
                .andExpect(jsonPath("$.longitudCentroide").isNumber())
                // RNF Seguridad: No deben exponerse las coordenadas originales del usuario
                .andExpect(jsonPath("$.latitud").doesNotExist())
                .andExpect(jsonPath("$.longitud").doesNotExist());
    }

    @Test
    @DisplayName("HU-04: Resolución de celda H3 por catálogo manual")
    void resolverCelda_ConZonaManual_Exitoso() throws Exception {
        ResolucionZonaSolicitud solicitud = ResolucionZonaSolicitud.builder()
                .zonaManualId("ZONA-USAQUEN")
                .build();

        mockMvc.perform(post("/api/v1/geo/resolver-celda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCeldaH3").isNotEmpty())
                .andExpect(jsonPath("$.nombreZona").value("Usaquén"))
                .andExpect(jsonPath("$.zonaManualId").value("ZONA-USAQUEN"))
                .andExpect(jsonPath("$.esManual").value(true));
    }

    @Test
    @DisplayName("HU-04: Rechazo con HTTP 400 si no se envían coordenadas ni zona manual")
    void resolverCelda_SinUbicacion_Rechazado() throws Exception {
        ResolucionZonaSolicitud solicitud = new ResolucionZonaSolicitud();

        mockMvc.perform(post("/api/v1/geo/resolver-celda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("UBICACION_REQUERIDA"));
    }

    @Test
    @DisplayName("HU-04: Rechazo con HTTP 404 si la zona manual no existe")
    void resolverCelda_ZonaManualInexistente_Error404() throws Exception {
        ResolucionZonaSolicitud solicitud = ResolucionZonaSolicitud.builder()
                .zonaManualId("ZONA-NO-EXISTE")
                .build();

        mockMvc.perform(post("/api/v1/geo/resolver-celda")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapeadorObjetos.writeValueAsString(solicitud)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ZONA_NO_ENCONTRADA"));
    }
}
