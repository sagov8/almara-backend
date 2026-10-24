package com.almara.modules.institutional.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.LineaDetalleDto;
import com.almara.modules.institutional.dto.OrdenSuscripcionRespuesta;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.CarritoCompra;
import com.almara.modules.institutional.models.LineaDetalle;
import com.almara.modules.institutional.models.OrdenPedido;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioCarritoCompra;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servicio de Aplicación para el proceso de Checkout, Selección en Carrito y Generación de Órdenes (HU-22).
 */
@Service
public class ServicioCheckoutSuscripcion {

    private static final Logger log = LoggerFactory.getLogger(ServicioCheckoutSuscripcion.class);

    private final RepositorioCarritoCompra repositorioCarritoCompra;
    private final RepositorioOrdenPedido repositorioOrdenPedido;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;

    public ServicioCheckoutSuscripcion(RepositorioCarritoCompra repositorioCarritoCompra,
                                       RepositorioOrdenPedido repositorioOrdenPedido,
                                       RepositorioPlanSuscripcion repositorioPlanSuscripcion,
                                       RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
                                       ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional) {
        this.repositorioCarritoCompra = repositorioCarritoCompra;
        this.repositorioOrdenPedido = repositorioOrdenPedido;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
    }

    /**
     * Selecciona un plan en el carrito del cliente institucional y genera la orden de suscripción atómicamente.
     * Garantiza cálculo financiero 100% en backend e idempotencia ante dobles clics.
     */
    @Transactional
    public OrdenSuscripcionRespuesta seleccionarPlanEnCarritoYGenerarOrden(String authorizationHeader, AdquirirPlanSolicitud solicitud) {
        ClaimsTokenInstitucional claims = extraerYValidarClaims(authorizationHeader);
        UUID idInstitucion = claims.getIdUsuario();

        PlanSuscripcion plan = repositorioPlanSuscripcion.findByCodigoIgnoreCase(solicitud.getCodigoPlan())
                .filter(PlanSuscripcion::isActivo)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "PLAN_NO_ENCONTRADO",
                        "El plan de suscripción especificado no existe o está inactivo"
                ));

        // 1. Gestionar Carrito de Compra (Agregación CarritoCompra o-- ItemCarrito)
        CarritoCompra carrito = repositorioCarritoCompra.findByIdInstitucion(idInstitucion)
                .orElseGet(() -> CarritoCompra.builder()
                        .idCarrito(UUID.randomUUID())
                        .idInstitucion(idInstitucion)
                        .fechaCreacion(Instant.now())
                        .build());

        carrito.agregarItem(plan, 1);
        repositorioCarritoCompra.save(carrito);

        // 2. Verificar Idempotencia: ¿Existe ya una orden pendiente para este plan y esta institución?
        Optional<OrdenPedido> ordenPendienteOpt = repositorioOrdenPedido
                .findFirstByIdInstitucionAndIdPlanSeleccionadoAndEstadoOrden(idInstitucion, plan.getIdProducto(), "PENDIENTE_PAGO");

        if (ordenPendienteOpt.isPresent()) {
            OrdenPedido existente = ordenPendienteOpt.get();
            log.info("Idempotencia activada: retornando orden existente {} para cliente {}", existente.getNumeroOrden(), idInstitucion);
            return mapearAOrdenRespuesta(existente, plan);
        }

        // 3. Crear nueva Orden de Pedido con Composición Fuerte de Línea de Detalle (OrdenPedido *-- LineaDetalle)
        UUID idOrden = UUID.randomUUID();
        String numeroOrden = "ORD-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        OrdenPedido orden = OrdenPedido.builder()
                .idOrden(idOrden)
                .numeroOrden(numeroOrden)
                .idInstitucion(idInstitucion)
                .idPlanSeleccionado(plan.getIdProducto())
                .fechaEmision(Instant.now())
                .estadoOrden("PENDIENTE_PAGO")
                .montoTotal(plan.calcularPrecioFinal())
                .build();

        LineaDetalle lineaDetalle = LineaDetalle.builder()
                .idLinea(UUID.randomUUID())
                .ordenPedido(orden)
                .idProducto(plan.getIdProducto())
                .concepto("Suscripción SaaS Almara - " + plan.getNombre() + " (" + plan.getDuracionDias() + " días)")
                .cantidad(1)
                .precioAplicado(plan.calcularPrecioFinal())
                .subtotal(plan.calcularPrecioFinal())
                .build();

        orden.agregarLineaDetalle(lineaDetalle);
        orden.confirmarCheckout();

        OrdenPedido guardada = repositorioOrdenPedido.save(orden);

        // 4. Registrar auditoría del checkout
        AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                .idAuditoria(UUID.randomUUID())
                .idEntidadAfectada(guardada.getIdOrden())
                .tipoEvento("ORDEN_CREADA")
                .detalle("Orden " + guardada.getNumeroOrden() + " generada para plan " + plan.getCodigo() + " por valor de $" + guardada.getMontoTotal())
                .actorResponsable(claims.getCorreo() != null ? claims.getCorreo() : idInstitucion.toString())
                .fechaEvento(Instant.now())
                .build();
        repositorioAuditoriaInstitucional.save(auditoria);

        log.info("Orden {} generada con éxito para la institución {}", guardada.getNumeroOrden(), idInstitucion);
        return mapearAOrdenRespuesta(guardada, plan);
    }

    /**
     * Consulta el detalle de una orden de pedido validando el aislamiento multi-tenant.
     */
    @Transactional(readOnly = true)
    public OrdenSuscripcionRespuesta consultarOrdenPorId(String authorizationHeader, UUID idOrden) {
        ClaimsTokenInstitucional claims = extraerYValidarClaims(authorizationHeader);
        UUID idInstitucion = claims.getIdUsuario();

        OrdenPedido orden = repositorioOrdenPedido.findById(idOrden)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "ORDEN_NO_ENCONTRADA",
                        "La orden de suscripción solicitada no existe"
                ));

        if (!orden.getIdInstitucion().equals(idInstitucion)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "No tiene permisos para consultar esta orden"
            );
        }

        PlanSuscripcion plan = repositorioPlanSuscripcion.findById(orden.getIdPlanSeleccionado())
                .orElse(null);

        return mapearAOrdenRespuesta(orden, plan);
    }

    private ClaimsTokenInstitucional extraerYValidarClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.UNAUTHORIZED,
                    "AUTENTICACION_REQUERIDA",
                    "Se requiere un token de acceso Bearer válido"
            );
        }
        String token = authorizationHeader.substring(7).trim();
        return proveedorTokenJwtInstitucional.validarYExtraerClaims(token);
    }

    private OrdenSuscripcionRespuesta mapearAOrdenRespuesta(OrdenPedido orden, PlanSuscripcion plan) {
        String codigoPlan = plan != null ? plan.getCodigo() : "DESCONOCIDO";
        String nombrePlan = plan != null ? plan.getNombre() : "Suscripción SaaS Almara";
        int duracionDias = plan != null ? plan.getDuracionDias() : 30;

        List<LineaDetalleDto> lineasDto = new ArrayList<>();
        if (orden.getLineasDetalle() != null) {
            lineasDto = orden.getLineasDetalle().stream()
                    .map(l -> LineaDetalleDto.builder()
                            .idLinea(l.getIdLinea())
                            .concepto(l.getConcepto())
                            .cantidad(l.getCantidad())
                            .precioAplicado(l.getPrecioAplicado())
                            .subtotal(l.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return OrdenSuscripcionRespuesta.builder()
                .idOrden(orden.getIdOrden())
                .numeroOrden(orden.getNumeroOrden())
                .idInstitucion(orden.getIdInstitucion())
                .idPlanSeleccionado(orden.getIdPlanSeleccionado())
                .codigoPlan(codigoPlan)
                .nombrePlan(nombrePlan)
                .duracionDias(duracionDias)
                .fechaEmision(orden.getFechaEmision())
                .estadoOrden(orden.getEstadoOrden())
                .montoTotal(orden.getMontoTotal())
                .lineasDetalle(lineasDto)
                .siguientePaso("PROCESAR_PAGO")
                .build();
    }
}
