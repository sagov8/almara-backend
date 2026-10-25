package com.almara.modules.admin.controllers;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.ActualizarResolucionH3Solicitud;
import com.almara.modules.admin.dto.ConfiguracionEspacialH3Respuesta;
import com.almara.modules.admin.dto.CrearPoligonoUrbanoSolicitud;
import com.almara.modules.admin.dto.ModificarPoligonoUrbanoSolicitud;
import com.almara.modules.admin.dto.PoligonoUrbanoRespuesta;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.admin.security.ProveedorTokenJwtAdmin;
import com.almara.modules.admin.services.ServicioGestionZonasEspacialesAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la administración de zonas geográficas H3 y polígonos urbanos (HU-14).
 * Acceso exclusivo para administradores autenticados con rol ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/v1/admin/geo")
@CrossOrigin(origins = "*")
public class ControladorZonasEspacialesAdmin {

    private static final Logger log = LoggerFactory.getLogger(ControladorZonasEspacialesAdmin.class);

    private final ServicioGestionZonasEspacialesAdmin servicioZonasEspaciales;
    private final ProveedorTokenJwtAdmin proveedorTokenJwtAdmin;

    public ControladorZonasEspacialesAdmin(ServicioGestionZonasEspacialesAdmin servicioZonasEspaciales,
                                          ProveedorTokenJwtAdmin proveedorTokenJwtAdmin) {
        this.servicioZonasEspaciales = servicioZonasEspaciales;
        this.proveedorTokenJwtAdmin = proveedorTokenJwtAdmin;
    }

    /**
     * Consulta la configuración vigente de resolución espacial H3.
     */
    @GetMapping("/resolucion")
    public ResponseEntity<ConfiguracionEspacialH3Respuesta> obtenerConfiguracionResolucion(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/geo/resolucion");
        return ResponseEntity.ok(servicioZonasEspaciales.obtenerConfiguracionResolucionH3());
    }

    /**
     * Actualiza la resolución H3 por defecto del sistema (rango 7 a 10).
     */
    @PutMapping("/resolucion")
    public ResponseEntity<ConfiguracionEspacialH3Respuesta> actualizarResolucionH3(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ActualizarResolucionH3Solicitud solicitud,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(authorization);
        String ipOrigen = extraerIpCliente(request);
        log.info("PUT /api/v1/admin/geo/resolucion -> admin='{}', nuevaRes={}, ip='{}'",
                admin.getCorreo(), solicitud.resolucionH3Default(), ipOrigen);

        ConfiguracionEspacialH3Respuesta respuesta = servicioZonasEspaciales.actualizarResolucionH3Default(
                solicitud, admin, ipOrigen
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista todos los polígonos espaciales urbanos registrados.
     */
    @GetMapping("/poligonos")
    public ResponseEntity<List<PoligonoUrbanoRespuesta>> listarPoligonos(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/geo/poligonos");
        return ResponseEntity.ok(servicioZonasEspaciales.listarPoligonosUrbanos());
    }

    /**
     * Obtiene el detalle de un polígono urbano específico.
     */
    @GetMapping("/poligonos/{idZona}")
    public ResponseEntity<PoligonoUrbanoRespuesta> obtenerPoligono(
            @PathVariable String idZona,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        exigirAdministrador(authorization);
        log.info("GET /api/v1/admin/geo/poligonos/{}", idZona);
        return ResponseEntity.ok(servicioZonasEspaciales.obtenerPoligonoPorId(idZona));
    }

    /**
     * Registra un nuevo polígono urbano espacial delimitado (GeoJSON cerrado).
     */
    @PostMapping("/poligonos")
    public ResponseEntity<PoligonoUrbanoRespuesta> crearPoligono(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CrearPoligonoUrbanoSolicitud solicitud,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(authorization);
        String ipOrigen = extraerIpCliente(request);
        log.info("POST /api/v1/admin/geo/poligonos -> id='{}', nombre='{}', ip='{}'",
                solicitud.idZona(), solicitud.nombreZona(), ipOrigen);

        PoligonoUrbanoRespuesta respuesta = servicioZonasEspaciales.crearPoligonoUrbano(
                solicitud, admin, ipOrigen
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Modifica un polígono urbano existente recalculando su centroide e indexación H3.
     */
    @PutMapping("/poligonos/{idZona}")
    public ResponseEntity<PoligonoUrbanoRespuesta> modificarPoligono(
            @PathVariable String idZona,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ModificarPoligonoUrbanoSolicitud solicitud,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(authorization);
        String ipOrigen = extraerIpCliente(request);
        log.info("PUT /api/v1/admin/geo/poligonos/{} -> admin='{}', ip='{}'", idZona, admin.getCorreo(), ipOrigen);

        PoligonoUrbanoRespuesta respuesta = servicioZonasEspaciales.modificarPoligonoUrbano(
                idZona, solicitud, admin, ipOrigen
        );
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Desactiva (borrado lógico) un polígono urbano en la base espacial.
     */
    @DeleteMapping("/poligonos/{idZona}")
    public ResponseEntity<PoligonoUrbanoRespuesta> desactivarPoligono(
            @PathVariable String idZona,
            @RequestParam(required = false) String justificacion,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        ClaimsTokenAdmin admin = exigirAdministrador(authorization);
        String ipOrigen = extraerIpCliente(request);
        log.info("DELETE /api/v1/admin/geo/poligonos/{} -> admin='{}', ip='{}'", idZona, admin.getCorreo(), ipOrigen);

        PoligonoUrbanoRespuesta respuesta = servicioZonasEspaciales.desactivarPoligonoUrbano(
                idZona, justificacion, admin, ipOrigen
        );
        return ResponseEntity.ok(respuesta);
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
                    "Se requiere rol ROLE_ADMIN para ajustar zonas geográficas espaciales."
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
