package com.almara.modules.institutional.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.dto.PagoSuscripcionRespuesta;
import com.almara.modules.institutional.dto.ProcesarPagoSolicitud;
import com.almara.modules.institutional.models.AuditoriaInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.models.OrdenPedido;
import com.almara.modules.institutional.models.Pago;
import com.almara.modules.institutional.models.PlanSuscripcion;
import com.almara.modules.institutional.models.SuscripcionInstitucional;
import com.almara.modules.institutional.ports.PasarelaPagoPort;
import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import com.almara.modules.institutional.repositories.RepositorioAuditoriaInstitucional;
import com.almara.modules.institutional.repositories.RepositorioCarritoCompra;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioOrdenPedido;
import com.almara.modules.institutional.repositories.RepositorioPago;
import com.almara.modules.institutional.repositories.RepositorioPlanSuscripcion;
import com.almara.modules.institutional.repositories.RepositorioSuscripcionInstitucional;
import com.almara.modules.institutional.security.ClaimsTokenInstitucional;
import com.almara.modules.institutional.security.ProveedorTokenJwtInstitucional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de Aplicación para el procesamiento y reintento de pagos de suscripciones SaaS (HU-23).
 * Orquesta la arquitectura hexagonal (PasarelaPagoPort, ServicioNotificacionPort) y transiciones atómicas.
 */
@Service
public class ServicioPagoSuscripcion {

    private static final Logger log = LoggerFactory.getLogger(ServicioPagoSuscripcion.class);

    private final RepositorioPago repositorioPago;
    private final RepositorioOrdenPedido repositorioOrdenPedido;
    private final RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional;
    private final RepositorioPlanSuscripcion repositorioPlanSuscripcion;
    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioCarritoCompra repositorioCarritoCompra;
    private final RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional;
    private final PasarelaPagoPort pasarelaPagoPort;
    private final ServicioNotificacionPort servicioNotificacionPort;
    private final ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional;

    public ServicioPagoSuscripcion(RepositorioPago repositorioPago,
                                   RepositorioOrdenPedido repositorioOrdenPedido,
                                   RepositorioSuscripcionInstitucional repositorioSuscripcionInstitucional,
                                   RepositorioPlanSuscripcion repositorioPlanSuscripcion,
                                   RepositorioClienteInstitucional repositorioClienteInstitucional,
                                   RepositorioCarritoCompra repositorioCarritoCompra,
                                   RepositorioAuditoriaInstitucional repositorioAuditoriaInstitucional,
                                   PasarelaPagoPort pasarelaPagoPort,
                                   ServicioNotificacionPort servicioNotificacionPort,
                                   ProveedorTokenJwtInstitucional proveedorTokenJwtInstitucional) {
        this.repositorioPago = repositorioPago;
        this.repositorioOrdenPedido = repositorioOrdenPedido;
        this.repositorioSuscripcionInstitucional = repositorioSuscripcionInstitucional;
        this.repositorioPlanSuscripcion = repositorioPlanSuscripcion;
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioCarritoCompra = repositorioCarritoCompra;
        this.repositorioAuditoriaInstitucional = repositorioAuditoriaInstitucional;
        this.pasarelaPagoPort = pasarelaPagoPort;
        this.servicioNotificacionPort = servicioNotificacionPort;
        this.proveedorTokenJwtInstitucional = proveedorTokenJwtInstitucional;
    }

    /**
     * Procesa el cobro de una orden de pedido a través de la pasarela de pagos desacoplada.
     */
    @Transactional
    public PagoSuscripcionRespuesta procesarPago(String authorizationHeader, ProcesarPagoSolicitud solicitud) {
        ClaimsTokenInstitucional claims = extraerYValidarClaims(authorizationHeader);
        UUID idInstitucion = claims.getIdUsuario();

        OrdenPedido orden = repositorioOrdenPedido.findById(solicitud.getIdOrden())
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "ORDEN_NO_ENCONTRADA",
                        "La orden de suscripción solicitada no existe"
                ));

        if (!orden.getIdInstitucion().equals(idInstitucion)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "No tiene permisos para procesar pagos de esta orden"
            );
        }

        if ("PAGADA".equalsIgnoreCase(orden.getEstadoOrden())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "ORDEN_YA_PAGADA",
                    "La orden de suscripción ya fue pagada exitosamente con anterioridad"
            );
        }

        // Ejecutar cobro mediante el puerto de pasarela de pagos
        Pago pago = pasarelaPagoPort.procesarCobro(orden, solicitud.getMetodoPago(), solicitud.getTokenMedioPago());
        Pago pagoGuardado = repositorioPago.save(pago);

        PlanSuscripcion plan = repositorioPlanSuscripcion.findById(orden.getIdPlanSeleccionado())
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "PLAN_NO_ENCONTRADO",
                        "El plan asociado a la orden no existe"
                ));

        ClienteInstitucional cliente = repositorioClienteInstitucional.findById(idInstitucion).orElse(null);
        String correoResponsable = cliente != null ? cliente.getCorreo() : claims.getCorreo();

        if (pago.procesarTransaccion()) {
            // 1. Transición de la orden a PAGADA
            orden.actualizarEstado("PAGADA");
            repositorioOrdenPedido.save(orden);

            // 2. Activación / Actualización de la Suscripción Institucional
            Instant ahora = Instant.now();
            Instant nuevaExpiracion = ahora.plus(plan.getDuracionDias(), ChronoUnit.DAYS);

            SuscripcionInstitucional suscripcion = repositorioSuscripcionInstitucional
                    .findFirstByIdInstitucionOrderByFechaExpiracionDesc(idInstitucion)
                    .orElseGet(() -> SuscripcionInstitucional.builder()
                            .idSuscripcion(UUID.randomUUID())
                            .idInstitucion(idInstitucion)
                            .idPlan(plan.getIdProducto())
                            .estadoSuscripcion("ACTIVA")
                            .fechaInicio(ahora)
                            .fechaExpiracion(nuevaExpiracion)
                            .build());

            suscripcion.setIdPlan(plan.getIdProducto());
            suscripcion.setEstadoSuscripcion("ACTIVA");
            suscripcion.setFechaInicio(ahora);
            suscripcion.setFechaExpiracion(nuevaExpiracion);
            suscripcion.setFechaActualizacion(ahora);
            repositorioSuscripcionInstitucional.save(suscripcion);

            // 3. Limpiar carrito de compras tras compra exitosa
            repositorioCarritoCompra.findByIdInstitucion(idInstitucion).ifPresent(c -> {
                if (c.getItems() != null) {
                    c.getItems().clear();
                    repositorioCarritoCompra.save(c);
                }
            });

            // 4. Emitir notificación transaccional (<<include>> Emitir Notificación de Suscripción)
            String asunto = "Almara — Confirmación de Pago y Activación de Suscripción (" + plan.getNombre() + ")";
            String cuerpo = "Estimado cliente institucional,\n\n"
                    + "Su pago por valor de $" + pago.getMontoPagado() + " ha sido confirmado exitosamente.\n"
                    + "Referencia externa: " + pago.getReferenciaExterna() + "\n"
                    + "Plan activado: " + plan.getNombre() + " (" + plan.getCodigo() + ")\n"
                    + "Vigencia: hasta " + nuevaExpiracion + ".\n\n"
                    + "Gracias por confiar en la plataforma Almara.";

            servicioNotificacionPort.enviarCorreoConfirmacion(correoResponsable, asunto, cuerpo);

            // 5. Registrar evento de auditoría
            AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                    .idAuditoria(UUID.randomUUID())
                    .idEntidadAfectada(pagoGuardado.getIdPago())
                    .tipoEvento("PAGO_APROBADO_Y_SUSCRIPCION_ACTIVADA")
                    .detalle("Pago aprobado por $" + pago.getMontoPagado() + " para orden " + orden.getNumeroOrden() + ". Plan activado: " + plan.getCodigo())
                    .actorResponsable(correoResponsable)
                    .fechaEvento(ahora)
                    .build();
            repositorioAuditoriaInstitucional.save(auditoria);

            // 6. Emitir JWT actualizado para refresh instantáneo en frontend
            String tokenActualizado = null;
            if (cliente != null) {
                tokenActualizado = proveedorTokenJwtInstitucional.generarToken(cliente, plan.getCodigo(), "ACTIVA");
            }

            log.info("Pago aprobado y suscripción activada para cliente {}. Orden {}", idInstitucion, orden.getNumeroOrden());

            return PagoSuscripcionRespuesta.builder()
                    .idPago(pagoGuardado.getIdPago())
                    .idOrden(orden.getIdOrden())
                    .numeroOrden(orden.getNumeroOrden())
                    .referenciaExterna(pagoGuardado.getReferenciaExterna())
                    .montoPagado(pagoGuardado.getMontoPagado())
                    .metodoPago(pagoGuardado.getMetodoPago())
                    .estadoPago("APROBADO")
                    .motivoRechazo(null)
                    .pagoAprobado(true)
                    .permiteReintento(false)
                    .estadoOrden("PAGADA")
                    .codigoPlanActivado(plan.getCodigo())
                    .estadoSuscripcion("ACTIVA")
                    .nuevaFechaExpiracion(nuevaExpiracion)
                    .notificacionEnviada(true)
                    .tokenAccesoActualizado(tokenActualizado)
                    .mensaje("Pago procesado y aprobado con éxito. Suscripción SaaS activada.")
                    .build();

        } else {
            // Transición de la orden a FALLIDA ante rechazo
            orden.actualizarEstado("FALLIDA");
            repositorioOrdenPedido.save(orden);

            AuditoriaInstitucional auditoria = AuditoriaInstitucional.builder()
                    .idAuditoria(UUID.randomUUID())
                    .idEntidadAfectada(pagoGuardado.getIdPago())
                    .tipoEvento("PAGO_RECHAZADO")
                    .detalle("Pago rechazado para orden " + orden.getNumeroOrden() + ". Motivo: " + pago.getMotivoRechazo())
                    .actorResponsable(correoResponsable)
                    .fechaEvento(Instant.now())
                    .build();
            repositorioAuditoriaInstitucional.save(auditoria);

            log.warn("Pago rechazado para orden {}. Motivo: {}", orden.getNumeroOrden(), pago.getMotivoRechazo());

            return PagoSuscripcionRespuesta.builder()
                    .idPago(pagoGuardado.getIdPago())
                    .idOrden(orden.getIdOrden())
                    .numeroOrden(orden.getNumeroOrden())
                    .referenciaExterna(pagoGuardado.getReferenciaExterna())
                    .montoPagado(pagoGuardado.getMontoPagado())
                    .metodoPago(pagoGuardado.getMetodoPago())
                    .estadoPago("RECHAZADO")
                    .motivoRechazo(pagoGuardado.getMotivoRechazo())
                    .pagoAprobado(false)
                    .permiteReintento(true)
                    .estadoOrden("FALLIDA")
                    .codigoPlanActivado(null)
                    .estadoSuscripcion(null)
                    .nuevaFechaExpiracion(null)
                    .notificacionEnviada(false)
                    .tokenAccesoActualizado(null)
                    .mensaje("La transacción fue rechazada por la pasarela de pagos. Puede reintentar con otro medio de pago.")
                    .build();
        }
    }

    /**
     * Caso de uso extendido: Reintentar Pago Fallido (<<extend>> Reintentar Pago Fallido).
     */
    @Transactional
    public PagoSuscripcionRespuesta reintentarPagoFallido(String authorizationHeader, ProcesarPagoSolicitud solicitud) {
        ClaimsTokenInstitucional claims = extraerYValidarClaims(authorizationHeader);
        UUID idInstitucion = claims.getIdUsuario();

        OrdenPedido orden = repositorioOrdenPedido.findById(solicitud.getIdOrden())
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "ORDEN_NO_ENCONTRADA",
                        "La orden de suscripción solicitada no existe"
                ));

        if (!orden.getIdInstitucion().equals(idInstitucion)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "No tiene permisos para reintentar el pago de esta orden"
            );
        }

        if ("PAGADA".equalsIgnoreCase(orden.getEstadoOrden())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "ORDEN_YA_PAGADA",
                    "La orden de suscripción ya fue pagada exitosamente con anterioridad"
            );
        }

        log.info("Ejecutando reintento de pago para orden {} en estado actual {}", orden.getNumeroOrden(), orden.getEstadoOrden());
        return procesarPago(authorizationHeader, solicitud);
    }

    /**
     * Consulta el historial de intentos de pago de una orden.
     */
    @Transactional(readOnly = true)
    public List<Pago> listarPagosDeOrden(String authorizationHeader, UUID idOrden) {
        ClaimsTokenInstitucional claims = extraerYValidarClaims(authorizationHeader);
        UUID idInstitucion = claims.getIdUsuario();

        OrdenPedido orden = repositorioOrdenPedido.findById(idOrden)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "ORDEN_NO_ENCONTRADA",
                        "La orden solicitada no existe"
                ));

        if (!orden.getIdInstitucion().equals(idInstitucion)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.FORBIDDEN,
                    "ACCESO_DENEGADO",
                    "No tiene permisos para consultar los pagos de esta orden"
            );
        }

        return repositorioPago.findByIdOrdenOrderByFechaTransaccionDesc(idOrden);
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
}
