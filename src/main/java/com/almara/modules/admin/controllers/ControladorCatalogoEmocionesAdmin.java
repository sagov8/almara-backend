package com.almara.modules.admin.controllers;

import com.almara.modules.admin.dto.CrearEmocionSolicitud;
import com.almara.modules.admin.dto.EmocionAdminRespuesta;
import com.almara.modules.admin.dto.ModificarEmocionSolicitud;
import com.almara.modules.admin.services.ServicioGestionCatalogoEmocionesAdmin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión del catálogo de emociones por el Administrador (HU-12).
 * Cubre los casos de uso:
 * - <<include>> Agregar
 * - <<include>> Modificar
 * - <<include>> Desactivar (borrado lógico con State Pattern)
 * - <<include>> Registrar Log de Auditoría
 */
@RestController
@RequestMapping("/api/v1/admin/emociones")
@CrossOrigin(origins = "*")
public class ControladorCatalogoEmocionesAdmin {

    private static final Logger log = LoggerFactory.getLogger(ControladorCatalogoEmocionesAdmin.class);

    private final ServicioGestionCatalogoEmocionesAdmin servicioGestionCatalogoEmocionesAdmin;

    public ControladorCatalogoEmocionesAdmin(ServicioGestionCatalogoEmocionesAdmin servicioGestionCatalogoEmocionesAdmin) {
        this.servicioGestionCatalogoEmocionesAdmin = servicioGestionCatalogoEmocionesAdmin;
    }

    /**
     * Consulta todas las emociones del catálogo (activas e inactivas) con sus métricas.
     */
    @GetMapping
    public ResponseEntity<List<EmocionAdminRespuesta>> listarEmociones(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("GET /api/v1/admin/emociones");
        List<EmocionAdminRespuesta> lista = servicioGestionCatalogoEmocionesAdmin.listarTodasLasEmociones(authorization);
        return ResponseEntity.ok(lista);
    }

    /**
     * Da de alta una nueva emoción predeterminada en el catálogo (HU-12: include Agregar).
     */
    @PostMapping
    public ResponseEntity<EmocionAdminRespuesta> agregarEmocion(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody CrearEmocionSolicitud solicitud,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("POST /api/v1/admin/emociones -> id='{}', etiqueta='{}', ip='{}'",
                solicitud.getIdEmocion(), solicitud.getNombreEtiqueta(), ipOrigen);

        EmocionAdminRespuesta respuesta = servicioGestionCatalogoEmocionesAdmin.agregarEmocion(solicitud, authorization, ipOrigen);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Modifica atributos visuales o etiquetas de una emoción existente (HU-12: include Modificar).
     */
    @PutMapping("/{idEmocion}")
    public ResponseEntity<EmocionAdminRespuesta> modificarEmocion(
            @PathVariable String idEmocion,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ModificarEmocionSolicitud solicitud,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("PUT /api/v1/admin/emociones/{} -> etiqueta='{}', color='{}'",
                idEmocion, solicitud.getNombreEtiqueta(), solicitud.getCodigoHexColor());

        EmocionAdminRespuesta respuesta = servicioGestionCatalogoEmocionesAdmin.modificarEmocion(
                idEmocion, solicitud, authorization, ipOrigen);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Desactiva una emoción mediante borrado lógico (HU-12: include Desactivar).
     */
    @PatchMapping("/{idEmocion}/desactivar")
    public ResponseEntity<EmocionAdminRespuesta> desactivarEmocion(
            @PathVariable String idEmocion,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("PATCH /api/v1/admin/emociones/{}/desactivar", idEmocion);

        EmocionAdminRespuesta respuesta = servicioGestionCatalogoEmocionesAdmin.desactivarEmocion(
                idEmocion, authorization, ipOrigen);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Reactiva una emoción inactiva en el catálogo.
     */
    @PatchMapping("/{idEmocion}/activar")
    public ResponseEntity<EmocionAdminRespuesta> activarEmocion(
            @PathVariable String idEmocion,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        String ipOrigen = extraerIpCliente(request);
        log.info("PATCH /api/v1/admin/emociones/{}/activar", idEmocion);

        EmocionAdminRespuesta respuesta = servicioGestionCatalogoEmocionesAdmin.activarEmocion(
                idEmocion, authorization, ipOrigen);
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
