package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.EstadoSuscripcionRespuesta;
import com.almara.modules.institutional.services.ServicioSuscripcionInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la gestión y consulta del Estado de Suscripciones Institucionales (HU-21).
 */
@RestController
@RequestMapping("/api/v1/institutional/suscripciones")
@CrossOrigin(origins = "*")
public class ControladorSuscripcionInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ControladorSuscripcionInstitucional.class);

    private final ServicioSuscripcionInstitucional servicioSuscripcionInstitucional;

    public ControladorSuscripcionInstitucional(ServicioSuscripcionInstitucional servicioSuscripcionInstitucional) {
        this.servicioSuscripcionInstitucional = servicioSuscripcionInstitucional;
    }

    /**
     * Consulta el estado de suscripción y licencia SaaS del cliente institucional autenticado (HU-21).
     */
    @GetMapping("/mi-estado")
    public ResponseEntity<EstadoSuscripcionRespuesta> consultarMiEstado(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("GET /api/v1/institutional/suscripciones/mi-estado");

        EstadoSuscripcionRespuesta respuesta =
                servicioSuscripcionInstitucional.consultarEstadoSuscripcionActual(authorization);
        return ResponseEntity.ok(respuesta);
    }
}
