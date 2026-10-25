package com.almara.modules.admin.controllers;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.*;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.admin.security.ProveedorTokenJwtAdmin;
import com.almara.modules.admin.services.ServicioGestionInstitucionalAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la gestión de usuarios y clientes institucionales (HU-15).
 * Permite a los administradores autenticados (ROLE_ADMIN) listar, aprobar cuentas, suspender,
 * dar de baja definitiva, reactivar, revocar tokens/sesiones y consultar planes disponibles.
 */
@RestController
@RequestMapping("/api/v1/admin/instituciones")
@CrossOrigin(origins = "*")
public class ControladorGestionInstitucionalAdmin {

    private static final Logger log = LoggerFactory.getLogger(ControladorGestionInstitucionalAdmin.class);

    private final ServicioGestionInstitucionalAdmin servicioGestionInstitucional;
    private final ProveedorTokenJwtAdmin proveedorTokenJwtAdmin;

    public ControladorGestionInstitucionalAdmin(
            ServicioGestionInstitucionalAdmin servicioGestionInstitucional,
            ProveedorTokenJwtAdmin proveedorTokenJwtAdmin) {
        this.servicioGestionInstitucional = servicioGestionInstitucional;
        this.proveedorTokenJwtAdmin = proveedorTokenJwtAdmin;
    }

    /**
     * Lista las instituciones registradas con filtros opcionales de estado y búsqueda.
     */
    @GetMapping
    public ResponseEntity<List<InstitucionAdminResumenDto>> listarInstituciones(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestParam(value = "busqueda", required = false) String busqueda) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/instituciones -> estado='{}', busqueda='{}'", estado, busqueda);
        List<InstitucionAdminResumenDto> lista = servicioGestionInstitucional.listarInstituciones(estado, busqueda);
        return ResponseEntity.ok(lista);
    }

    /**
     * Consulta el perfil y estado detallado de una institución por su UUID.
     */
    @GetMapping("/{idInstitucion}")
    public ResponseEntity<InstitucionAdminResumenDto> obtenerDetalleInstitucion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/instituciones/{}", idInstitucion);
        InstitucionAdminResumenDto detalle = servicioGestionInstitucional.obtenerDetalle(idInstitucion);
        return ResponseEntity.ok(detalle);
    }

    /**
     * Lista los planes SaaS disponibles para asignación institucional.
     */
    @GetMapping("/planes")
    public ResponseEntity<List<PlanAdminResumenDto>> listarPlanesDisponibles(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/instituciones/planes");
        List<PlanAdminResumenDto> planes = servicioGestionInstitucional.listarPlanesDisponibles();
        return ResponseEntity.ok(planes);
    }

    /**
     * <<include>> Aprobar Cuenta Institucional:
     * Transiciona la cuenta a ACTIVA, asocia o confirma el plan contratado,
     * despacha correo SMTP de bienvenida y registra evento de auditoría.
     */
    @PostMapping("/{idInstitucion}/aprobar")
    public ResponseEntity<InstitucionAdminResumenDto> aprobarInstitucion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion,
            @Valid @RequestBody(required = false) AprobarInstitucionSolicitud solicitud,
            HttpServletRequest request) {
        ClaimsTokenAdmin claims = exigirAdministrador(authorization);
        String ip = extraerIpCliente(request);
        log.info("POST /api/v1/admin/instituciones/{}/aprobar -> admin='{}', ip='{}'",
                idInstitucion, claims.getCorreo(), ip);

        InstitucionAdminResumenDto respuesta = servicioGestionInstitucional.aprobarInstitucion(
                idInstitucion, solicitud, claims.getCorreo(), claims.getIdAdmin(), ip
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * <<include>> Suspender / Dar de Baja Cuenta -> Suspender:
     * Cambia el estado a SUSPENDIDA, revoca de inmediato todas las sesiones activas,
     * despacha correo de notificación y registra log de auditoría.
     */
    @PostMapping("/{idInstitucion}/suspender")
    public ResponseEntity<InstitucionAdminResumenDto> suspenderInstitucion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion,
            @Valid @RequestBody SuspenderInstitucionSolicitud solicitud,
            HttpServletRequest request) {
        ClaimsTokenAdmin claims = exigirAdministrador(authorization);
        String ip = extraerIpCliente(request);
        log.info("POST /api/v1/admin/instituciones/{}/suspender -> admin='{}', motivo='{}'",
                idInstitucion, claims.getCorreo(), solicitud.getMotivo());

        InstitucionAdminResumenDto respuesta = servicioGestionInstitucional.suspenderInstitucion(
                idInstitucion, solicitud.getMotivo(), claims.getCorreo(), claims.getIdAdmin(), ip
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * <<include>> Suspender / Dar de Baja Cuenta -> Dar de Baja:
     * Cambia el estado a INACTIVA de manera definitiva, revoca de inmediato sesiones/tokens,
     * notifica por correo institucional y registra log de auditoría.
     */
    @PostMapping("/{idInstitucion}/dar-de-baja")
    public ResponseEntity<InstitucionAdminResumenDto> darDeBajaInstitucion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion,
            @Valid @RequestBody DarDeBajaInstitucionSolicitud solicitud,
            HttpServletRequest request) {
        ClaimsTokenAdmin claims = exigirAdministrador(authorization);
        String ip = extraerIpCliente(request);
        log.info("POST /api/v1/admin/instituciones/{}/dar-de-baja -> admin='{}', motivo='{}'",
                idInstitucion, claims.getCorreo(), solicitud.getMotivo());

        InstitucionAdminResumenDto respuesta = servicioGestionInstitucional.darDeBajaInstitucion(
                idInstitucion, solicitud.getMotivo(), claims.getCorreo(), claims.getIdAdmin(), ip
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Reactiva una cuenta institucional previamente suspendida o inactiva a estado ACTIVA.
     */
    @PostMapping("/{idInstitucion}/reactivar")
    public ResponseEntity<InstitucionAdminResumenDto> reactivarInstitucion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion,
            @Valid @RequestBody(required = false) ReactivarInstitucionSolicitud solicitud,
            HttpServletRequest request) {
        ClaimsTokenAdmin claims = exigirAdministrador(authorization);
        String ip = extraerIpCliente(request);
        String motivo = solicitud != null ? solicitud.getMotivo() : null;
        log.info("POST /api/v1/admin/instituciones/{}/reactivar -> admin='{}', motivo='{}'",
                idInstitucion, claims.getCorreo(), motivo);

        InstitucionAdminResumenDto respuesta = servicioGestionInstitucional.reactivarInstitucion(
                idInstitucion, motivo, claims.getCorreo(), claims.getIdAdmin(), ip
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * <<include>> Revocar Tokens y Sesiones Activas:
     * Invalida de inmediato todos los tokens de acceso JWT y finaliza sesiones concurrentes.
     */
    @PostMapping("/{idInstitucion}/revocar-sesiones")
    public ResponseEntity<Map<String, Object>> revocarSesionesActivas(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("idInstitucion") UUID idInstitucion,
            @Valid @RequestBody(required = false) RevocarSesionesInstitucionSolicitud solicitud,
            HttpServletRequest request) {
        ClaimsTokenAdmin claims = exigirAdministrador(authorization);
        String ip = extraerIpCliente(request);
        String motivo = solicitud != null ? solicitud.getMotivo() : null;
        log.info("POST /api/v1/admin/instituciones/{}/revocar-sesiones -> admin='{}'",
                idInstitucion, claims.getCorreo());

        servicioGestionInstitucional.revocarSesionesActivas(
                idInstitucion, motivo, claims.getCorreo(), claims.getIdAdmin(), ip
        );

        return ResponseEntity.ok(Map.of(
                "mensaje", "Sesiones y tokens JWT revocados exitosamente para la institución",
                "idInstitucion", idInstitucion.toString()
        ));
    }

    private ClaimsTokenAdmin exigirAdministrador(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "Token de autorización administrativo requerido (ROLE_ADMIN)."
            );
        }
        String token = authorization.substring(7).trim();
        ClaimsTokenAdmin claims = proveedorTokenJwtAdmin.validarYExtraerClaims(token);
        if (!"ROLE_ADMIN".equalsIgnoreCase(claims.getRol())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ROL_INSUFICIENTE",
                    "Se requiere rol ROLE_ADMIN para administrar usuarios institucionales."
            );
        }
        return claims;
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
