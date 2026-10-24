package com.almara.modules.institutional.security;

import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.MatrizPermisoModulo;
import com.almara.modules.institutional.models.ModuloSistema;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioMatrizPermisoModulo;
import com.almara.modules.institutional.repositories.RepositorioModuloSistema;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Motor centralizado de validación y control de acceso por módulos y plan activo (HU-20).
 * Valida autorizaciones en tiempo real contra la base de datos y la matriz dinámica de permisos.
 */
@Component
public class ValidadorPermisosBackend {

    private static final Logger log = LoggerFactory.getLogger(ValidadorPermisosBackend.class);

    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioModuloSistema repositorioModuloSistema;
    private final RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;

    public ValidadorPermisosBackend(RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
                                    RepositorioClienteInstitucional repositorioClienteInstitucional,
                                    RepositorioPlanSuscripcion repositorioPlanSuscripcion,
                                    RepositorioModuloSistema repositorioModuloSistema,
                                    RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo,
                                    RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
                                    ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional) {
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioModuloSistema = repositorioModuloSistema;
        this.repositorioMatrizPermisoModulo = repositorioMatrizPermisoModulo;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
    }

    /**
     * Valida permisos para una suscripción e identificador de módulo según el diagrama UML.
     */
    @Transactional
    public boolean autorizarPeticion(UUID idSuscripcion, String codigoModulo) {
        Optional<SuscripcionInstitucional> suscripcionOpt = repositorioSuscripcionInstitucional.findById(idSuscripcion);
        if (suscripcionOpt.isEmpty()) {
            return false;
        }

        SuscripcionInstitucional suscripcion = suscripcionOpt.get();
        if (!suscripcion.estaVigente()) {
            suscripcion.revocarPorExpiracion();
            repositorioSuscripcionInstitucional.save(suscripcion);
            auditarCambioEstado(idSuscripcion, "EXPIRADA");
            return false;
        }

        Optional<ModuloSistema> moduloOpt = repositorioModuloSistema.findByCodigoModuloIgnoreCase(codigoModulo);
        if (moduloOpt.isEmpty()) {
            return false;
        }

        Optional<MatrizPermisoModulo> permisoOpt = repositorioMatrizPermisoModulo
                .findByIdPlanAndIdModulo(suscripcion.getIdPlan(), moduloOpt.get().getIdModulo());

        return permisoOpt.map(MatrizPermisoModulo::isModuloHabilitado).orElse(false);
    }

    /**
     * Registra en la bitácora de auditoría los cambios de estado de la suscripción (RNF-13).
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void auditarCambioEstado(UUID idSuscripcion, String nuevoEstado) {
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(idSuscripcion)
                .tipoEvento("ESTADO_SUSCRIPCION_ACTUALIZADO")
                .detalle("Suscripción actualizada a estado " + nuevoEstado + " por verificación de vigencia")
                .actorResponsable("MOTOR_SEGURIDAD_HU20")
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.saveAndFlush(auditoria);
        log.info("[AUDITORIA HU-20] Suscripción {} actualizada a estado {}", idSuscripcion, nuevoEstado);
    }

    /**
     * Valida el acceso de una petición HTTP autorizada por token JWT a un módulo específico del sistema.
     * Lanza ExcepcionModuloRestringidoPorPlan (403 FORBIDDEN) si la suscripción expiró o el plan no incluye el módulo.
     */
    @Transactional(noRollbackFor = {ExcepcionModuloRestringidoPorPlan.class})
    public ResultadoValidacionModulo validarAccesoModuloPorToken(String authorizationHeader, String codigoModuloRequerido) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ExcepcionModuloRestringidoPorPlan(
                    "AUTENTICACION_REQUERIDA",
                    "Se requiere un token de acceso Bearer válido"
            );
        }

        String token = authorizationHeader.substring(7).trim();
        ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);
        UUID idInstitucion = claims.getIdUsuario();

        // 1. Verificar estado de la cuenta institucional
        ClienteInstitucional cliente = repositorioClienteInstitucional.findById(idInstitucion)
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "CLIENTE_NO_ENCONTRADO",
                        "No se encontró el registro institucional asociado al token"
                ));

        if (!cliente.isActivo()) {
            throw new ExcepcionModuloRestringidoPorPlan(
                    "CUENTA_NO_ACTIVA",
                    "La cuenta institucional se encuentra inactiva o pendiente de verificación"
            );
        }

        // 2. Verificar Suscripción Institucional en base de datos
        SuscripcionInstitucional suscripcion = repositorioSuscripcionInstitucional
                .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "SUSCRIPCION_NO_ENCONTRADA",
                        "No se encontró una suscripción institucional registrada para esta entidad"
                ));

        // 3. Verificar Vigencia (fecha de expiración y estado)
        if (!suscripcion.estaVigente()) {
            suscripcion.revocarPorExpiracion();
            repositorioSuscripcionInstitucional.save(suscripcion);
            auditarCambioEstado(suscripcion.getIdSuscripcion(), "EXPIRADA");

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("codigoModulo", codigoModuloRequerido);
            detalles.put("estadoSuscripcion", "EXPIRADA");
            detalles.put("fechaExpiracion", suscripcion.getFechaExpiracion());

            throw new ExcepcionModuloRestringidoPorPlan(
                    "SUSCRIPCION_EXPIRADA",
                    "Su suscripción institucional ha expirado. Renueve su plan para continuar accediendo a los módulos del sistema.",
                    detalles
            );
        }

        // 4. Verificar Módulo del Sistema Solicitado
        ModuloSistema modulo = repositorioModuloSistema.findByCodigoModuloIgnoreCase(codigoModuloRequerido)
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "MODULO_NO_ENCONTRADO",
                        "El módulo solicitado (" + codigoModuloRequerido + ") no existe en el catálogo del sistema"
                ));

        // 5. Verificar Plan de Suscripción y Matriz de Permisos Dinámica
        PlanSuscripcion plan = repositorioPlanSuscripcion.findById(suscripcion.getIdPlan())
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "PLAN_NO_ENCONTRADO",
                        "El plan configurado en la suscripción no existe"
                ));

        Optional<MatrizPermisoModulo> permisoOpt = repositorioMatrizPermisoModulo
                .findByIdPlanAndIdModulo(plan.getIdProducto(), modulo.getIdModulo());

        boolean habilitado = permisoOpt.map(MatrizPermisoModulo::isModuloHabilitado).orElse(false);

        if (!habilitado) {
            // Registrar intento bloqueado en auditoría
            AuditoriaInstitucional auditoriaBloqueo = AuditoriaInstitucional.builder()
                    .idAuditoria(UUID.randomUUID())
                    .idEntidadAfectada(suscripcion.getIdSuscripcion())
                    .tipoEvento("ACCESO_MODULO_DENEGADO")
                    .detalle("Acceso denegado a módulo " + codigoModuloRequerido + " para cliente " + cliente.getRazonSocial() + " con plan " + plan.getCodigo())
                    .actorResponsable(cliente.getCorreo())
                    .fechaEvento(Instant.now())
                    .build();
            repositorioAuditoriaInstitucional.save(auditoriaBloqueo);

            Map<String, Object> detalles = new HashMap<>();
            detalles.put("codigoModulo", codigoModuloRequerido);
            detalles.put("nombreModulo", modulo.getNombreModulo());
            detalles.put("planActual", plan.getCodigo());
            detalles.put("estadoSuscripcion", suscripcion.getEstadoSuscripcion());
            detalles.put("planesRecomendados", List.of("PLAN_STANDARD", "PLAN_ENTERPRISE"));

            throw new ExcepcionModuloRestringidoPorPlan(
                    "MODULO_RESTRINGIDO_POR_PLAN",
                    "El módulo " + codigoModuloRequerido + " (" + modulo.getNombreModulo() + ") requiere un plan superior (PLAN_STANDARD o PLAN_ENTERPRISE).",
                    detalles
            );
        }

        return new ResultadoValidacionModulo(
                idInstitucion,
                suscripcion.getIdSuscripcion(),
                plan.getCodigo(),
                suscripcion.getEstadoSuscripcion(),
                codigoModuloRequerido,
                true,
                suscripcion.getFechaExpiracion()
        );
    }
}
