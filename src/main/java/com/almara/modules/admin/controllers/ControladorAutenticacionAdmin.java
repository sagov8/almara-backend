package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.LoginAdminRespuesta;
import com.almara.modules.admin.dto.LoginAdminSolicitud;
import com.almara.modules.admin.dto.LogoutAdminRespuesta;
import com.almara.modules.admin.dto.SesionAdminRespuesta;
import com.almara.modules.admin.services.ServicioAutenticacionAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el flujo de autenticación del Administrador (HU-11, HU-18).
 * Expone endpoints seguros para iniciar sesión, verificar sesión activa y cerrar sesión.
 */
@RestController
@RequestMapping("/api/v1/admin/auth")
@CrossOrigin(origins = "*")
public class ControladorAutenticacionAdmin {

    private static final Logger log = LoggerFactory.getLogger(ControladorAutenticacionAdmin.class);

    private final ServicioAutenticacionAdmin servicioAutenticacionAdmin;

    public ControladorAutenticacionAdmin(ServicioAutenticacionAdmin servicioAutenticacionAdmin) {
        this.servicioAutenticacionAdmin = servicioAutenticacionAdmin;
    }

    /**
     * Inicia sesión como administrador en la plataforma Almara (HU-11).
     * Incluye validación de credenciales criptográficas y emisión de token JWT con rol ROLE_ADMIN.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginAdminRespuesta> iniciarSesion(
            @Valid @RequestBody LoginAdminSolicitud solicitud,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("POST /api/v1/admin/auth/login -> usuario='{}', ip='{}'",
                solicitud.getUsuarioAdmin(), ipOrigen);

        LoginAdminRespuesta respuesta = servicioAutenticacionAdmin.iniciarSesion(solicitud, ipOrigen);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Consulta el estado de la sesión activa del administrador (HU-11, HU-18).
     */
    @GetMapping("/sesion-actual")
    public ResponseEntity<SesionAdminRespuesta> obtenerSesionActual(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("GET /api/v1/admin/auth/sesion-actual");
        SesionAdminRespuesta respuesta = servicioAutenticacionAdmin.obtenerSesionActual(authorization);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Cierra la sesión activa del administrador y revoca su token JWT (HU-18: include Revocar Tokens y Sesiones Activas).
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutAdminRespuesta> cerrarSesion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("POST /api/v1/admin/auth/logout -> ip='{}'", ipOrigen);

        LogoutAdminRespuesta respuesta = servicioAutenticacionAdmin.cerrarSesion(authorization, ipOrigen);
        return ResponseEntity.ok(respuesta);
    }

    private String extraerIpCliente(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String headerXForwarded = request.getHeader("X-Forwarded-For");
        if (headerXForwarded != null && !headerXForwarded.isBlank()) {
            return headerXForwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
