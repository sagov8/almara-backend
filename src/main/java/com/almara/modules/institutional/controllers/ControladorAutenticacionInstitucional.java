package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.RegistroInstitucionalRespuesta;
import com.almara.modules.institutional.dto.RegistroInstitucionalSolicitud;
import com.almara.modules.institutional.dto.VerificacionCorreoRespuesta;
import com.almara.modules.institutional.dto.VerificacionCorreoSolicitud;
import com.almara.modules.institutional.services.ServicioRegistroInstitucional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el flujo de Autenticación y Registro Institucional (HU-16).
 */
@RestController
@RequestMapping("/api/v1/institutional/auth")
@CrossOrigin(origins = "*")
public class ControladorAutenticacionInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ControladorAutenticacionInstitucional.class);

    private final ServicioRegistroInstitucional servicioRegistroInstitucional;

    public ControladorAutenticacionInstitucional(ServicioRegistroInstitucional servicioRegistroInstitucional) {
        this.servicioRegistroInstitucional = servicioRegistroInstitucional;
    }

    /**
     * Registra una nueva entidad institucional (HU-16).
     */
    @PostMapping("/registro")
    public ResponseEntity<RegistroInstitucionalRespuesta> registrarCuenta(
            @Valid @RequestBody RegistroInstitucionalSolicitud solicitud) {
        log.info("POST /api/v1/institutional/auth/registro -> razonSocial='{}', correo='{}'",
                solicitud.getRazonSocial(), solicitud.getCorreoCorporativo());

        RegistroInstitucionalRespuesta respuesta = servicioRegistroInstitucional.registrarCuenta(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Verifica el correo institucional y activa la cuenta asignando PLAN_FREE por defecto.
     */
    @PostMapping("/verificar-correo")
    public ResponseEntity<VerificacionCorreoRespuesta> verificarCorreo(
            @Valid @RequestBody VerificacionCorreoSolicitud solicitud) {
        log.info("POST /api/v1/institutional/auth/verificar-correo -> token=[{}]", solicitud.getTokenVerificacion());

        VerificacionCorreoRespuesta respuesta = servicioRegistroInstitucional.verificarCorreoYActivarCuenta(solicitud);
        return ResponseEntity.ok(respuesta);
    }
}
