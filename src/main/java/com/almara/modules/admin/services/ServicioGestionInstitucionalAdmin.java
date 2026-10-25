package com.almara.modules.admin.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.*;
import com.almara.modules.admin.models.state.institutional.EstadoCuentaInstitucionalState;
import com.almara.modules.admin.models.state.institutional.FabricaEstadoInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.ServicioRevocacionToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para la Gestión de Usuarios Institucionales por parte del Administrador (HU-15).
 * Coordina el ciclo de vida (State Pattern), revocación de tokens (Token Blacklist),
 * notificaciones (Ports & Adapters) y auditoría inmutable.
 */
@Service
public class ServicioGestionInstitucionalAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioGestionInstitucionalAdmin.class);

    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final ServicioNotificacionPort servicioNotificacionPort;
    private final ServicioRevocacionToken servicioRevocacionToken;
    private final ServicioAuditoriaAdmin servicioAuditoriaAdmin;

    public ServicioGestionInstitucionalAdmin(
            RepositorioClienteInstitucional repositorioClienteInstitucional,
            RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
            RepositorioPlanSuscripcion repositorioPlanSuscripcion,
            ServicioNotificacionPort servicioNotificacionPort,
            ServicioRevocacionToken servicioRevocacionToken,
            ServicioAuditoriaAdmin servicioAuditoriaAdmin) {
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.servicioNotificacionPort = servicioNotificacionPort;
        this.servicioRevocacionToken = servicioRevocacionToken;
        this.servicioAuditoriaAdmin = servicioAuditoriaAdmin;
    }

    /**
     * Lista todos los clientes institucionales aplicando filtros opcionales de estado y búsqueda textual.
     */
    @Transactional(readOnly = true)
    public List<InstitucionAdminResumenDto> listarInstituciones(String estado, String busqueda) {
        List<ClienteInstitucional> clientes = repositorioClienteInstitucional.findAll();

        return clientes.stream()
                .filter(c -> {
                    if (estado == null || estado.isBlank() || "TODOS".equalsIgnoreCase(estado)) {
                        return true;
                    }
                    return estado.trim().equalsIgnoreCase(c.getEstadoCuenta());
                })
                .filter(c -> {
                    if (busqueda == null || busqueda.isBlank()) {
                        return true;
                    }
                    String q = busqueda.trim().toLowerCase();
                    boolean matchRazon = c.getRazonSocial() != null && c.getRazonSocial().toLowerCase().contains(q);
                    boolean matchNit = c.getIdentificacionFiscalNit() != null && c.getIdentificacionFiscalNit().toLowerCase().contains(q);
                    boolean matchCorreo = c.getCorreo() != null && c.getCorreo().toLowerCase().contains(q);
                    return matchRazon || matchNit || matchCorreo;
                })
                .sorted(Comparator.comparing(ClienteInstitucional::getFechaRegistro, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el detalle completo de una institución por su UUID.
     */
    @Transactional(readOnly = true)
    public InstitucionAdminResumenDto obtenerDetalle(UUID idInstitucion) {
        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);
        return mapearADto(cliente);
    }

    /**
     * <<include>> Aprobar Cuenta Institucional:
     * Transiciona la cuenta de PENDIENTE_APROBACION a ACTIVA, asocia el plan y envía notificación.
     */
    @Transactional
    public InstitucionAdminResumenDto aprobarInstitucion(UUID idInstitucion, AprobarInstitucionSolicitud solicitud,
                                                        String adminEmail, UUID idAdmin, String ip) {
        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);
        EstadoCuentaInstitucionalState estadoActual = FabricaEstadoInstitucional.obtenerEstado(cliente.getEstadoCuenta());

        PlanSuscripcion plan = resolverPlan(solicitud, idInstitucion);

        try {
            estadoActual.aprobar(cliente, plan, solicitud != null ? solicitud.getMotivo() : null);
        } catch (IllegalStateException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.CONFLICT, "TRANSICION_ESTADO_INVALIDA", ex.getMessage());
        }

        // Crear o actualizar la suscripción a estado ACTIVA
        Optional<SuscripcionInstitucional> suscripcionOpt = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion);

        SuscripcionInstitucional suscripcion;
        Instant ahora = Instant.now();
        Instant expira = ahora.plusSeconds((long) plan.getDuracionDias() * 86400L);

        if (suscripcionOpt.isPresent()) {
            suscripcion = suscripcionOpt.get();
            suscripcion.setIdPlan(plan.getIdProducto());
            suscripcion.setEstadoSuscripcion("ACTIVA");
            suscripcion.setFechaInicio(ahora);
            suscripcion.setFechaExpiracion(expira);
            suscripcion.setFechaActualizacion(ahora);
        } else {
            suscripcion = new SuscripcionInstitucional(
                    UUID.randomUUID(), idInstitucion, plan.getIdProducto(), "ACTIVA", ahora, expira, ahora
            );
        }
        repositorioSuscripcionInstitucional.save(suscripcion);
        repositorioClienteInstitucional.save(cliente);

        // Notificación por correo (SMTP) desacoplada con tolerancia a fallos
        despacharNotificacionSegura(
                cliente.getCorreo(),
                "Almara — Cuenta Institucional Aprobada",
                "Estimado(a) representante de " + cliente.getRazonSocial() + ",\n\n"
                        + "Nos complace informarle que su registro institucional ha sido revisado y formalmente APROBADO por la administración de Almara.\n\n"
                        + "Detalles de su cuenta:\n"
                        + "• Entidad: " + cliente.getRazonSocial() + "\n"
                        + "• Plan Asignado: " + plan.getNombre() + " (" + plan.getTipoPlan() + ")\n"
                        + "• Vigencia: Hasta " + expira.toString() + "\n\n"
                        + "Ya puede iniciar sesión con sus credenciales registradas para ingresar a la plataforma y explorar el panel de analítica emocional colectiva de Popayán.\n\n"
                        + "Atentamente,\nEquipo de Administración y Seguridad Almara."
        );

        // Registro de Auditoría Admin
        String detalleAuditoria = "Aprobada cuenta institucional [" + cliente.getRazonSocial()
                + "] (NIT: " + cliente.getIdentificacionFiscalNit() + ") asignando plan [" + plan.getNombre() + "]"
                + (solicitud != null && solicitud.getMotivo() != null && !solicitud.getMotivo().isBlank()
                ? ". Justificación: " + solicitud.getMotivo().trim() : "");
        servicioAuditoriaAdmin.registrarEvento(idAdmin, "INSTITUCION_APROBADA", detalleAuditoria, adminEmail, ip);

        log.info("Institución [{}] aprobada con éxito por admin [{}]", cliente.getIdUsuario(), adminEmail);
        return mapearADto(cliente);
    }

    /**
     * <<include>> Suspender / Dar de Baja Cuenta -> Suspender:
     * Transiciona la cuenta a SUSPENDIDA, revoca de inmediato todas las sesiones activas y notifica por correo.
     */
    @Transactional
    public InstitucionAdminResumenDto suspenderInstitucion(UUID idInstitucion, String motivo,
                                                          String adminEmail, UUID idAdmin, String ip) {
        if (motivo == null || motivo.trim().isBlank()) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.BAD_REQUEST, "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo o justificación técnica para suspender la cuenta.");
        }

        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);
        EstadoCuentaInstitucionalState estadoActual = FabricaEstadoInstitucional.obtenerEstado(cliente.getEstadoCuenta());

        try {
            estadoActual.suspender(cliente, motivo.trim());
        } catch (IllegalStateException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.CONFLICT, "TRANSICION_ESTADO_INVALIDA", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
        }

        // <<include>> Revocar Tokens y Sesiones Activas
        servicioRevocacionToken.revocarSesionesUsuario(idInstitucion);

        // Actualizar suscripción si existe
        repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .ifPresent(s -> {
                    s.setEstadoSuscripcion("SUSPENDIDA");
                    s.setFechaActualizacion(Instant.now());
                    repositorioSuscripcionInstitucional.save(s);
                });

        repositorioClienteInstitucional.save(cliente);

        // <<include>> Enviar Notificación de Estado
        despacharNotificacionSegura(
                cliente.getCorreo(),
                "Almara — Suspensión Temporal de Cuenta Institucional",
                "Estimado(a) representante de " + cliente.getRazonSocial() + ",\n\n"
                        + "Le comunicamos que su cuenta institucional ha sido temporalmente SUSPENDIDA por la administración de Almara.\n\n"
                        + "Motivo o justificación: " + motivo.trim() + "\n\n"
                        + "Sus credenciales de acceso y sesiones activas han sido suspendidas temporalmente. "
                        + "Para mayor información o regularización, por favor comuníquese con el equipo de soporte.\n\n"
                        + "Atentamente,\nEquipo de Administración Almara."
        );

        // <<include>> Registrar Log de Auditoría
        String detalleAuditoria = "Suspendida cuenta institucional [" + cliente.getRazonSocial()
                + "] (NIT: " + cliente.getIdentificacionFiscalNit() + "). Motivo: " + motivo.trim()
                + ". Sesiones activas revocadas de inmediato.";
        servicioAuditoriaAdmin.registrarEvento(idAdmin, "INSTITUCION_SUSPENDIDA", detalleAuditoria, adminEmail, ip);

        log.info("Institución [{}] suspendida y sesiones revocadas por admin [{}]", idInstitucion, adminEmail);
        return mapearADto(cliente);
    }

    /**
     * <<include>> Suspender / Dar de Baja Cuenta -> Dar de Baja:
     * Transiciona la cuenta a INACTIVA de manera definitiva, revoca tokens/sesiones y notifica por correo.
     */
    @Transactional
    public InstitucionAdminResumenDto darDeBajaInstitucion(UUID idInstitucion, String motivo,
                                                          String adminEmail, UUID idAdmin, String ip) {
        if (motivo == null || motivo.trim().isBlank()) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.BAD_REQUEST, "MOTIVO_REQUERIDO",
                    "Debe indicar el motivo o justificación técnica para dar de baja la cuenta.");
        }

        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);
        EstadoCuentaInstitucionalState estadoActual = FabricaEstadoInstitucional.obtenerEstado(cliente.getEstadoCuenta());

        try {
            estadoActual.darDeBaja(cliente, motivo.trim());
        } catch (IllegalStateException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.CONFLICT, "TRANSICION_ESTADO_INVALIDA", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
        }

        // <<include>> Revocar Tokens y Sesiones Activas
        servicioRevocacionToken.revocarSesionesUsuario(idInstitucion);

        // Actualizar suscripción si existe
        repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .ifPresent(s -> {
                    s.setEstadoSuscripcion("INACTIVA");
                    s.setFechaActualizacion(Instant.now());
                    repositorioSuscripcionInstitucional.save(s);
                });

        repositorioClienteInstitucional.save(cliente);

        // <<include>> Enviar Notificación de Estado
        despacharNotificacionSegura(
                cliente.getCorreo(),
                "Almara — Notificación de Baja de Cuenta Institucional",
                "Estimado(a) representante de " + cliente.getRazonSocial() + ",\n\n"
                        + "Le informamos que su cuenta institucional en Almara ha sido dada de baja de manera definitiva.\n\n"
                        + "Motivo o justificación: " + motivo.trim() + "\n\n"
                        + "Todos los accesos, suscripciones y tokens han sido cancelados.\n\n"
                        + "Atentamente,\nEquipo de Administración Almara."
        );

        // <<include>> Registrar Log de Auditoría
        String detalleAuditoria = "Dada de baja definitiva cuenta institucional [" + cliente.getRazonSocial()
                + "] (NIT: " + cliente.getIdentificacionFiscalNit() + "). Motivo: " + motivo.trim()
                + ". Sesiones revocadas.";
        servicioAuditoriaAdmin.registrarEvento(idAdmin, "INSTITUCION_DADA_DE_BAJA", detalleAuditoria, adminEmail, ip);

        log.info("Institución [{}] dada de baja por admin [{}]", idInstitucion, adminEmail);
        return mapearADto(cliente);
    }

    /**
     * Reactiva una cuenta suspendida o inactiva a estado ACTIVA.
     */
    @Transactional
    public InstitucionAdminResumenDto reactivarInstitucion(UUID idInstitucion, String motivo,
                                                          String adminEmail, UUID idAdmin, String ip) {
        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);
        EstadoCuentaInstitucionalState estadoActual = FabricaEstadoInstitucional.obtenerEstado(cliente.getEstadoCuenta());

        try {
            estadoActual.reactivar(cliente, motivo != null ? motivo.trim() : null);
        } catch (IllegalStateException ex) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.CONFLICT, "TRANSICION_ESTADO_INVALIDA", ex.getMessage());
        }

        // Actualizar suscripción a ACTIVA si existía
        repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .ifPresent(s -> {
                    s.setEstadoSuscripcion("ACTIVA");
                    s.setFechaActualizacion(Instant.now());
                    repositorioSuscripcionInstitucional.save(s);
                });

        repositorioClienteInstitucional.save(cliente);

        despacharNotificacionSegura(
                cliente.getCorreo(),
                "Almara — Cuenta Institucional Reactivada",
                "Estimado(a) representante de " + cliente.getRazonSocial() + ",\n\n"
                        + "Nos complace notificarle que su cuenta institucional en Almara ha sido reactivada satisfactoriamente.\n"
                        + "Ya puede acceder nuevamente a su panel analítico corporativo.\n\n"
                        + "Atentamente,\nEquipo Almara."
        );

        String detalleAuditoria = "Reactivada cuenta institucional [" + cliente.getRazonSocial()
                + "] a estado ACTIVA"
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "");
        servicioAuditoriaAdmin.registrarEvento(idAdmin, "INSTITUCION_REACTIVADA", detalleAuditoria, adminEmail, ip);

        return mapearADto(cliente);
    }

    /**
     * <<include>> Revocar Tokens y Sesiones Activas (Ejecución forzada explícita).
     */
    @Transactional
    public void revocarSesionesActivas(UUID idInstitucion, String motivo, String adminEmail, UUID idAdmin, String ip) {
        ClienteInstitucional cliente = obtenerEntidadCliente(idInstitucion);

        servicioRevocacionToken.revocarSesionesUsuario(idInstitucion);

        String detalleAuditoria = "Revocación explícita de todas las sesiones y tokens para la entidad ["
                + cliente.getRazonSocial() + "]"
                + (motivo != null && !motivo.isBlank() ? ". Motivo: " + motivo.trim() : "");
        servicioAuditoriaAdmin.registrarEvento(idAdmin, "SESIONES_INSTITUCION_REVOCADAS", detalleAuditoria, adminEmail, ip);

        log.info("Sesiones forzosamente revocadas para institución [{}] por admin [{}]", idInstitucion, adminEmail);
    }

    /**
     * Lista los planes disponibles para la asignación institucional.
     */
    @Transactional(readOnly = true)
    public List<PlanAdminResumenDto> listarPlanesDisponibles() {
        return repositorioPlanSuscripcion.findAll().stream()
                .filter(PlanSuscripcion::isActivo)
                .map(p -> new PlanAdminResumenDto(
                        p.getIdProducto(),
                        p.getCodigo(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getPrecioBase(),
                        p.getTipoPlan(),
                        p.getDuracionDias(),
                        p.isEsGratuito(),
                        p.isActivo()
                ))
                .collect(Collectors.toList());
    }

    private ClienteInstitucional obtenerEntidadCliente(UUID idInstitucion) {
        return repositorioClienteInstitucional.findById(idInstitucion)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND, "INSTITUCION_NO_ENCONTRADA",
                        "No existe una cuenta institucional con el ID proporcionado: " + idInstitucion
                ));
    }

    private PlanSuscripcion resolverPlan(AprobarInstitucionSolicitud solicitud, UUID idInstitucion) {
        if (solicitud != null && solicitud.getIdPlan() != null) {
            return repositorioPlanSuscripcion.findById(solicitud.getIdPlan())
                    .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                            HttpStatus.NOT_FOUND, "PLAN_NO_ENCONTRADO",
                            "El plan de suscripción especificado no existe"
                    ));
        }

        if (solicitud != null && solicitud.getCodigoPlan() != null && !solicitud.getCodigoPlan().isBlank()) {
            return repositorioPlanSuscripcion.findByCodigoIgnoreCase(solicitud.getCodigoPlan().trim())
                    .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                            HttpStatus.NOT_FOUND, "PLAN_NO_ENCONTRADO",
                            "No existe un plan con el código: " + solicitud.getCodigoPlan()
                    ));
        }

        // Si ya tenía suscripción previa asociada, usar ese mismo plan
        Optional<SuscripcionInstitucional> suscripcionPrevia = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion);
        if (suscripcionPrevia.isPresent()) {
            return repositorioPlanSuscripcion.findById(suscripcionPrevia.get().getIdPlan())
                    .orElseGet(this::obtenerPlanFreePorDefecto);
        }

        return obtenerPlanFreePorDefecto();
    }

    private PlanSuscripcion obtenerPlanFreePorDefecto() {
        return repositorioPlanSuscripcion.findByCodigoIgnoreCase("PLAN_FREE")
                .orElseThrow(() -> new IllegalStateException("El plan base PLAN_FREE no está configurado en la base de datos"));
    }

    private void despacharNotificacionSegura(String destinatario, String asunto, String cuerpo) {
        try {
            servicioNotificacionPort.enviarCorreoConfirmacion(destinatario, asunto, cuerpo);
        } catch (Exception e) {
            log.error("Fallo al despachar notificación por correo a {}: {}", destinatario, e.getMessage(), e);
        }
    }

    private InstitucionAdminResumenDto mapearADto(ClienteInstitucional c) {
        Optional<SuscripcionInstitucional> subOpt = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(c.getIdUsuario());

        UUID idPlan = null;
        String codigoPlan = null;
        String nombrePlan = null;
        String estadoSuscripcion = null;
        Instant fechaExpiracion = null;

        if (subOpt.isPresent()) {
            SuscripcionInstitucional sub = subOpt.get();
            idPlan = sub.getIdPlan();
            estadoSuscripcion = sub.getEstadoSuscripcion();
            fechaExpiracion = sub.getFechaExpiracion();

            Optional<PlanSuscripcion> planOpt = repositorioPlanSuscripcion.findById(sub.getIdPlan());
            if (planOpt.isPresent()) {
                codigoPlan = planOpt.get().getCodigo();
                nombrePlan = planOpt.get().getNombre();
            }
        }

        return new InstitucionAdminResumenDto(
                c.getIdUsuario(),
                c.getRazonSocial(),
                c.getIdentificacionFiscalNit(),
                c.getCorreo(),
                c.getNombreContacto(),
                c.getTelefonoContacto(),
                c.getSectorActividad(),
                c.getEstadoCuenta(),
                c.isCorreoVerificado(),
                c.isActivo(),
                c.getFechaRegistro(),
                idPlan,
                codigoPlan,
                nombrePlan,
                estadoSuscripcion,
                fechaExpiracion
        );
    }
}
