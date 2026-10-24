package com.almara.modules.admin.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.CrearEmocionSolicitud;
import com.almara.modules.admin.dto.EmocionAdminRespuesta;
import com.almara.modules.admin.dto.ModificarEmocionSolicitud;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.admin.security.ProveedorTokenJwtAdmin;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocionJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de administración del Catálogo Oficial de Emociones (HU-12).
 * Orquesta los casos de uso incluidos:
 * - <<include>> Agregar
 * - <<include>> Modificar
 * - <<include>> Desactivar (State Pattern)
 * - <<include>> Registrar Log de Auditoría
 */
@Service
public class ServicioGestionCatalogoEmocionesAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioGestionCatalogoEmocionesAdmin.class);
    private static final int MINIMO_EMOCIONES_ACTIVAS = 3;

    private final RepositorioCatalogoEmocion repositorioCatalogoEmocion;
    private final RepositorioReporteEmocionJpa repositorioReporteEmocionJpa;
    private final ProveedorTokenJwtAdmin proveedorTokenJwtAdmin;
    private final ServicioAuditoriaAdmin servicioAuditoriaAdmin;

    public ServicioGestionCatalogoEmocionesAdmin(
            RepositorioCatalogoEmocion repositorioCatalogoEmocion,
            RepositorioReporteEmocionJpa repositorioReporteEmocionJpa,
            ProveedorTokenJwtAdmin proveedorTokenJwtAdmin,
            ServicioAuditoriaAdmin servicioAuditoriaAdmin) {
        this.repositorioCatalogoEmocion = repositorioCatalogoEmocion;
        this.repositorioReporteEmocionJpa = repositorioReporteEmocionJpa;
        this.proveedorTokenJwtAdmin = proveedorTokenJwtAdmin;
        this.servicioAuditoriaAdmin = servicioAuditoriaAdmin;
    }

    /**
     * Consulta todas las emociones registradas en el catálogo con sus métricas históricas de uso.
     */
    @Transactional(readOnly = true)
    public List<EmocionAdminRespuesta> listarTodasLasEmociones(String authorizationHeader) {
        validarRolAdmin(authorizationHeader);
        List<EntidadCatalogoEmocion> entidades = repositorioCatalogoEmocion.findAllByOrderByNombreEtiquetaAsc();
        return entidades.stream().map(this::mapearARespuesta).toList();
    }

    /**
     * Caso de uso incluido: `<<include>> Agregar` (HU-12).
     * Da de alta una nueva emoción predeterminada en el catálogo y registra la auditoría.
     */
    @Transactional
    public EmocionAdminRespuesta agregarEmocion(CrearEmocionSolicitud solicitud,
                                               String authorizationHeader,
                                               String ipOrigen) {
        ClaimsTokenAdmin claims = validarRolAdmin(authorizationHeader);

        String idNormalizado = solicitud.getIdEmocion().trim().toUpperCase();

        if (repositorioCatalogoEmocion.existsById(idNormalizado)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "EMOCION_YA_EXISTE",
                    "Ya existe una emoción registrada en el catálogo con el identificador '" + idNormalizado + "'");
        }

        String fondo = solicitud.getCodigoHexFondo();
        if (fondo == null || fondo.isBlank()) {
            fondo = solicitud.getCodigoHexColor();
        }

        EntidadCatalogoEmocion nueva = EntidadCatalogoEmocion.builder()
                .idEmocion(idNormalizado)
                .nombreEtiqueta(solicitud.getNombreEtiqueta().trim())
                .codigoHexColor(solicitud.getCodigoHexColor().toUpperCase().trim())
                .codigoHexFondo(fondo.toUpperCase().trim())
                .iconoSvg(solicitud.getIconoSvg().trim())
                .descripcion(solicitud.getDescripcion() != null ? solicitud.getDescripcion().trim() : "")
                .activo(true)
                .fechaCreacion(Instant.now())
                .build();

        EntidadCatalogoEmocion guardada = repositorioCatalogoEmocion.save(nueva);

        // Caso de uso incluido: <<include>> Registrar Log de Auditoría
        servicioAuditoriaAdmin.registrarEvento(
                claims.getIdAdmin(),
                "CATALOGO_AGREGAR_EMOCION",
                "Agregada emoción [" + idNormalizado + "] con etiqueta '" + guardada.getNombreEtiqueta() + "' y color " + guardada.getCodigoHexColor(),
                claims.getCorreo(),
                ipOrigen
        );

        log.info("Emoción agregada al catálogo por admin='{}': id='{}'", claims.getCorreo(), idNormalizado);
        return mapearARespuesta(guardada);
    }

    /**
     * Caso de uso incluido: `<<include>> Modificar` (HU-12).
     * Modifica atributos visuales y etiquetas manteniendo inmutable la clave primaria idEmocion.
     */
    @Transactional
    public EmocionAdminRespuesta modificarEmocion(String idEmocion,
                                                 ModificarEmocionSolicitud solicitud,
                                                 String authorizationHeader,
                                                 String ipOrigen) {
        ClaimsTokenAdmin claims = validarRolAdmin(authorizationHeader);
        String idNormalizado = idEmocion.trim().toUpperCase();

        EntidadCatalogoEmocion entidad = repositorioCatalogoEmocion.findById(idNormalizado)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "EMOCION_NO_ENCONTRADA",
                        "No se encontró ninguna emoción con el identificador '" + idNormalizado + "'"));

        String fondo = solicitud.getCodigoHexFondo();
        if (fondo == null || fondo.isBlank()) {
            fondo = solicitud.getCodigoHexColor();
        }

        entidad.setNombreEtiqueta(solicitud.getNombreEtiqueta().trim());
        entidad.setCodigoHexColor(solicitud.getCodigoHexColor().toUpperCase().trim());
        entidad.setCodigoHexFondo(fondo.toUpperCase().trim());
        entidad.setIconoSvg(solicitud.getIconoSvg().trim());
        if (solicitud.getDescripcion() != null) {
            entidad.setDescripcion(solicitud.getDescripcion().trim());
        }

        EntidadCatalogoEmocion actualizada = repositorioCatalogoEmocion.save(entidad);

        // Caso de uso incluido: <<include>> Registrar Log de Auditoría
        servicioAuditoriaAdmin.registrarEvento(
                claims.getIdAdmin(),
                "CATALOGO_MODIFICAR_EMOCION",
                "Modificada emoción [" + idNormalizado + "]: etiqueta='" + actualizada.getNombreEtiqueta() + "', color=" + actualizada.getCodigoHexColor(),
                claims.getCorreo(),
                ipOrigen
        );

        log.info("Emoción modificada en el catálogo por admin='{}': id='{}'", claims.getCorreo(), idNormalizado);
        return mapearARespuesta(actualizada);
    }

    /**
     * Caso de uso incluido: `<<include>> Desactivar` (HU-12).
     * Desactiva la emoción mediante borrado lógico utilizando el patrón State, validando el mínimo de 3 emociones activas.
     */
    @Transactional
    public EmocionAdminRespuesta desactivarEmocion(String idEmocion,
                                                   String authorizationHeader,
                                                   String ipOrigen) {
        ClaimsTokenAdmin claims = validarRolAdmin(authorizationHeader);
        String idNormalizado = idEmocion.trim().toUpperCase();

        EntidadCatalogoEmocion entidad = repositorioCatalogoEmocion.findById(idNormalizado)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "EMOCION_NO_ENCONTRADA",
                        "No se encontró ninguna emoción con el identificador '" + idNormalizado + "'"));

        // Si ya está inactiva, respuesta idempotente
        if (!Boolean.TRUE.equals(entidad.getActivo())) {
            return mapearARespuesta(entidad);
        }

        long totalActivas = repositorioCatalogoEmocion.countByActivoTrue();

        // Aplicación del Patrón State: valida mínimo de 3 activas (RF-05) y muta a inactivo
        entidad.obtenerEstado().desactivar(entidad, totalActivas, MINIMO_EMOCIONES_ACTIVAS);

        EntidadCatalogoEmocion guardada = repositorioCatalogoEmocion.save(entidad);

        // Caso de uso incluido: <<include>> Registrar Log de Auditoría
        servicioAuditoriaAdmin.registrarEvento(
                claims.getIdAdmin(),
                "CATALOGO_DESACTIVAR_EMOCION",
                "Desactivada emoción [" + idNormalizado + "] mediante borrado lógico (quedan " + (totalActivas - 1) + " activas)",
                claims.getCorreo(),
                ipOrigen
        );

        log.info("Emoción desactivada por admin='{}': id='{}'", claims.getCorreo(), idNormalizado);
        return mapearARespuesta(guardada);
    }

    /**
     * Reactiva una emoción inactiva en el catálogo mediante el patrón State.
     */
    @Transactional
    public EmocionAdminRespuesta activarEmocion(String idEmocion,
                                               String authorizationHeader,
                                               String ipOrigen) {
        ClaimsTokenAdmin claims = validarRolAdmin(authorizationHeader);
        String idNormalizado = idEmocion.trim().toUpperCase();

        EntidadCatalogoEmocion entidad = repositorioCatalogoEmocion.findById(idNormalizado)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "EMOCION_NO_ENCONTRADA",
                        "No se encontró ninguna emoción con el identificador '" + idNormalizado + "'"));

        // Aplicación del Patrón State
        entidad.obtenerEstado().activar(entidad);

        EntidadCatalogoEmocion guardada = repositorioCatalogoEmocion.save(entidad);

        // Caso de uso incluido: <<include>> Registrar Log de Auditoría
        servicioAuditoriaAdmin.registrarEvento(
                claims.getIdAdmin(),
                "CATALOGO_ACTIVAR_EMOCION",
                "Activada emoción [" + idNormalizado + "] en el catálogo oficial",
                claims.getCorreo(),
                ipOrigen
        );

        log.info("Emoción activada por admin='{}': id='{}'", claims.getCorreo(), idNormalizado);
        return mapearARespuesta(guardada);
    }

    private EmocionAdminRespuesta mapearARespuesta(EntidadCatalogoEmocion e) {
        long reportesAsociados = repositorioReporteEmocionJpa.countByIdEmocionIgnoreCase(e.getIdEmocion());
        return new EmocionAdminRespuesta(
                e.getIdEmocion(),
                e.getNombreEtiqueta(),
                e.getCodigoHexColor(),
                e.getCodigoHexFondo(),
                e.getIconoSvg(),
                e.getDescripcion(),
                Boolean.TRUE.equals(e.getActivo()),
                reportesAsociados,
                e.getFechaCreacion()
        );
    }

    private ClaimsTokenAdmin validarRolAdmin(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED, "AUTORIZACION_REQUERIDA", "Se requiere token administrativo para esta operación");
        }
        String token = authorizationHeader.substring(7).trim();
        ClaimsTokenAdmin claims = proveedorTokenJwtAdmin.validarYExtraerClaims(token);
        if (!"ROLE_ADMIN".equalsIgnoreCase(claims.getRol())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acción restringida exclusivamente a usuarios con rol ROLE_ADMIN");
        }
        return claims;
    }
}
