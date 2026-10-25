package com.almara.modules.admin.controllers;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.ActualizarUmbralMapaSolicitud;
import com.almara.modules.admin.dto.ConfiguracionUmbralMapaRespuesta;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.admin.security.ProveedorTokenJwtAdmin;
import com.almara.modules.admin.services.ServicioGestionUmbralesMapaAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controlador REST para la gestión de umbrales del mapa y reglas de privacidad (HU-13).
 * Restringido estrictamente a administradores con rol ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/mapa/umbrales")
@CrossOrigin(origins = "*")
public class ControladorUmbralesMapaAdmin {

    private static final Logger log = LoggerFactory.getLogger(ControladorUmbralesMapaAdmin.class);

    private final ServicioGestionUmbralesMapaAdmin servicioGestionUmbrales;
    private final ProveedorTokenJwtAdmin proveedorTokenJwtAdmin;

    public ControladorUmbralesMapaAdmin(ServicioGestionUmbralesMapaAdmin servicioGestionUmbrales,
                                       ProveedorTokenJwtAdmin proveedorTokenJwtAdmin) {
        this.servicioGestionUmbrales = servicioGestionUmbrales;
        this.proveedorTokenJwtAdmin = proveedorTokenJwtAdmin;
    }

    /**
     * Consulta la configuración vigente de umbrales de privacidad y k-anonimato del mapa.
     */
    @GetMapping
    public ResponseEntity<ConfiguracionUmbralMapaRespuesta> obtenerConfiguracion(
            @RequestHeader(value = "Authorization", required = false) String encabezadoAuth
    ) {
        exigirAdministrador(encabezadoAuth);
        log.info("GET /api/v1/admin/mapa/umbrales -> Consulta de configuración de privacidad");
        return ResponseEntity.ok(servicioGestionUmbrales.obtenerConfiguracion());
    }

    /**
     * Actualiza en caliente los umbrales del mapa (k-anonimato y periodicidad de recálculo).
     */
    @PutMapping
    public ResponseEntity<ConfiguracionUmbralMapaRespuesta> actualizarConfiguracion(
            @RequestHeader(value = "Authorization", required = false) String encabezadoAuth,
            @Valid @RequestBody ActualizarUmbralMapaSolicitud solicitud,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(encabezadoAuth);
        String ipOrigen = extraerIp(request);

        log.info("PUT /api/v1/admin/mapa/umbrales -> admin='{}', nuevoK={}, ip='{}'",
                admin.getCorreo(), solicitud.umbralMinimoK(), ipOrigen);

        ConfiguracionUmbralMapaRespuesta respuesta = servicioGestionUmbrales.actualizarConfiguracion(
                solicitud, admin, ipOrigen
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Restablece los umbrales a los parámetros de seguridad por defecto.
     */
    @PostMapping("/restablecer")
    public ResponseEntity<ConfiguracionUmbralMapaRespuesta> restablecerPorDefecto(
            @RequestHeader(value = "Authorization", required = false) String encabezadoAuth,
            @RequestBody(required = false) Map<String, String> cuerpo,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(encabezadoAuth);
        String ipOrigen = extraerIp(request);
        String justificacion = cuerpo != null ? cuerpo.get("justificacion") : null;

        log.info("POST /api/v1/admin/mapa/umbrales/restablecer -> admin='{}', ip='{}'", admin.getCorreo(), ipOrigen);

        ConfiguracionUmbralMapaRespuesta respuesta = servicioGestionUmbrales.restablecerPorDefecto(
                admin, justificacion, ipOrigen
        );
        return ResponseEntity.ok(respuesta);
    }

    private ClaimsTokenAdmin exigirAdministrador(String encabezadoAuth) {
        if (encabezadoAuth == null || !encabezadoAuth.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "Token de autorización administrativo requerido (ROLE_ADMIN)."
            );
        }
        String token = encabezadoAuth.substring(7).trim();
        ClaimsTokenAdmin claims = proveedorTokenJwtAdmin.validarYExtraerClaims(token);
        if (!"ROLE_ADMIN".equalsIgnoreCase(claims.getRol())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ROL_INSUFICIENTE",
                    "Se requiere rol ROLE_ADMIN para ajustar umbrales del mapa."
            );
        }
        return claims;
    }

    private String extraerIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
