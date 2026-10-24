package com.almara.modules.institutional.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.dto.EstadoSuscripcionRespuesta;
import com.almara.modules.institutional.dto.ResumenOrdenSuscripcionDto;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.MatrizPermisoModulo;
import com.almara.modules.institutional.models.ModuloSistema;
import com.almara.modules.institutional.models.OrdenPedido;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioMatrizPermisoModulo;
import com.almara.modules.institutional.repositories.RepositorioModuloSistema;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para la gestión y consulta del Estado de Suscripciones SaaS (HU-21).
 */
@Service
public class ServicioSuscripcionInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ServicioSuscripcionInstitucional.class);

    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioModuloSistema repositorioModuloSistema;
    private final RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo;
    private final RepositorioOrdenPedido repositorioOrdenPedido;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;

    public ServicioSuscripcionInstitucional(
            RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
            RepositorioClienteInstitucional repositorioClienteInstitucional,
            RepositorioPlanSuscripcion repositorioPlanSuscripcion,
            RepositorioModuloSistema repositorioModuloSistema,
            RepositorioMatrizPermisoModulo repositorioMatrizPermisoModulo,
            RepositorioOrdenPedido repositorioOrdenPedido,
            RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
            ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional) {
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioModuloSistema = repositorioModuloSistema;
        this.repositorioMatrizPermisoModulo = repositorioMatrizPermisoModulo;
        this.repositorioOrdenPedido = repositorioOrdenPedido;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
    }

    /**
     * Consulta el estado en tiempo real de la suscripción SaaS del cliente institucional autenticado (HU-21).
     */
    @Transactional
    public EstadoSuscripcionRespuesta consultarEstadoSuscripcionActual(String encabezadoAuthorization) {
        if (encabezadoAuthorization == null || !encabezadoAuthorization.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "AUTENTICACION_REQUERIDA",
                    "Se requiere un token de acceso Bearer válido para consultar el estado de la suscripción"
            );
        }

        String token = encabezadoAuthorization.substring(7).trim();
        ClaimsTokenInstitucional claims = proveedorTokenJwtInstitucional.validarYExtraerClaims(token);
        UUID idInstitucion = claims.getIdUsuario();

        ClienteInstitucional cliente = repositorioClienteInstitucional.findById(idInstitucion)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "CLIENTE_NO_ENCONTRADO",
                        "No se encontró la entidad institucional asociada al token"
                ));

        Optional<SuscripcionInstitucional> suscripcionOpt =
                repositorioSuscripcionInstitucional.findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion);

        if (suscripcionOpt.isEmpty()) {
            return EstadoSuscripcionRespuesta.builder()
                    .idInstitucion(idInstitucion)
                    .razonSocial(cliente.getRazonSocial())
                    .codigoPlan("SIN_PLAN")
                    .nombrePlan("Sin Plan Activo")
                    .precioPlan(BigDecimal.ZERO)
                    .esGratuito(true)
                    .estadoSuscripcion("INACTIVA")
                    .vigente(false)
                    .diasRestantes(0L)
                    .alertaVencimiento(true)
                    .mensajeEstado("La entidad no posee una suscripción activa. Por favor seleccione un plan del catálogo.")
                    .modulosHabilitados(new ArrayList<>())
                    .modulosRestringidos(new ArrayList<>())
                    .historialOrdenes(obtenerHistorialOrdenes(idInstitucion))
                    .build();
        }

        SuscripcionInstitucional suscripcion = suscripcionOpt.get();

        // Evaluación de vigencia en tiempo real
        if ("ACTIVA".equalsIgnoreCase(suscripcion.getEstadoSuscripcion()) && !suscripcion.estaVigente()) {
            suscripcion.revocarPorExpiracion();
            repositorioSuscripcionInstitucional.save(suscripcion);

            AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                    .idAuditoria(UUID.randomUUID())
                    .idEntidadAfectada(idInstitucion)
                    .tipoEvento("EXPIRACION_SUSCRIPCION")
                    .detalle("La suscripción institucional expiró automáticamente por vencimiento de plazo")
                    .actorResponsable(cliente.getCorreo())
                    .fechaEvento(Instant.now())
                    .build();
            repositorioAuditoriaInstitucional.save(auditoria);

            log.info("Suscripción {} revocada automáticamente por expiración para cliente {}",
                    suscripcion.getIdSuscripcion(), idInstitucion);
        }

        PlanSuscripcion plan = repositorioPlanSuscripcion.findById(suscripcion.getIdPlan())
                .orElse(null);

        String codigoPlan = plan != null ? plan.getCodigo() : "PLAN_FREE";
        String nombrePlan = plan != null ? plan.getNombre() : "Plan Básico";
        BigDecimal precioPlan = plan != null ? plan.calcularPrecioFinal() : BigDecimal.ZERO;
        boolean esGratuito = plan != null && plan.isEsGratuito();

        // Módulos habilitados y restringidos
        List<String> modulosHabilitados = new ArrayList<>();
        List<String> modulosRestringidos = new ArrayList<>();

        if (plan != null) {
            List<MatrizPermisoModulo> permisos = repositorioMatrizPermisoModulo.findByIdPlan(plan.getIdProducto());
            List<ModuloSistema> todosModulos = repositorioModuloSistema.findAll();
            Map<UUID, Boolean> estadoModulos = permisos.stream()
                    .collect(Collectors.toMap(MatrizPermisoModulo::getIdModulo, MatrizPermisoModulo::isModuloHabilitado, (a, b) -> a));

            for (ModuloSistema mod : todosModulos) {
                Boolean habilitado = estadoModulos.get(mod.getIdModulo());
                if (Boolean.TRUE.equals(habilitado) && suscripcion.estaVigente()) {
                    modulosHabilitados.add(mod.getCodigoModulo());
                } else {
                    modulosRestringidos.add(mod.getCodigoModulo());
                }
            }
        }

        long diasRestantes = suscripcion.calcularDiasRestantes();
        boolean alertaVencimiento = !suscripcion.estaVigente() || diasRestantes <= 5;

        String mensajeEstado;
        if (!suscripcion.estaVigente()) {
            mensajeEstado = "Su suscripción institucional ha expirado. Renueve su plan para restaurar el acceso a los módulos analíticos.";
        } else if (diasRestantes <= 5) {
            mensajeEstado = "Su suscripción vencerá en " + diasRestantes + " día(s). Le recomendamos renovar oportunamente para evitar interrupción de servicio.";
        } else {
            mensajeEstado = "Su suscripción se encuentra activa y vigente. Disfrute de los módulos habilitados para su entidad.";
        }

        return EstadoSuscripcionRespuesta.builder()
                .idSuscripcion(suscripcion.getIdSuscripcion())
                .idInstitucion(idInstitucion)
                .razonSocial(cliente.getRazonSocial())
                .codigoPlan(codigoPlan)
                .nombrePlan(nombrePlan)
                .precioPlan(precioPlan)
                .esGratuito(esGratuito)
                .estadoSuscripcion(suscripcion.getEstadoSuscripcion())
                .vigente(suscripcion.estaVigente())
                .fechaInicio(suscripcion.getFechaInicio())
                .fechaExpiracion(suscripcion.getFechaExpiracion())
                .diasRestantes(diasRestantes)
                .alertaVencimiento(alertaVencimiento)
                .mensajeEstado(mensajeEstado)
                .modulosHabilitados(modulosHabilitados)
                .modulosRestringidos(modulosRestringidos)
                .historialOrdenes(obtenerHistorialOrdenes(idInstitucion))
                .build();
    }

    private List<ResumenOrdenSuscripcionDto> obtenerHistorialOrdenes(UUID idInstitucion) {
        List<OrdenPedido> ordenes = repositorioOrdenPedido.findByIdInstitucionOrderByFechaEmisionDesc(idInstitucion);
        List<ResumenOrdenSuscripcionDto> dtos = new ArrayList<>();

        for (OrdenPedido orden : ordenes) {
            String nombrePlan = repositorioPlanSuscripcion.findById(orden.getIdPlanSeleccionado())
                    .map(PlanSuscripcion::getNombre)
                    .orElse("Suscripción SaaS Almara");

            dtos.add(ResumenOrdenSuscripcionDto.builder()
                    .idOrden(orden.getIdOrden())
                    .numeroOrden(orden.getNumeroOrden())
                    .nombrePlan(nombrePlan)
                    .montoTotal(orden.getMontoTotal())
                    .estadoOrden(orden.getEstadoOrden())
                    .fechaEmision(orden.getFechaEmision())
                    .build());
        }
        return dtos;
    }
}
