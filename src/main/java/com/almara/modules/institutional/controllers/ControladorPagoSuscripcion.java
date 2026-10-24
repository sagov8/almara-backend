package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.PagoSuscripcionRespuesta;
import com.almara.modules.institutional.dto.ProcesarPagoSolicitud;
import com.almara.modules.institutional.models.Pago;
import com.almara.modules.institutional.services.ServicioPagoSuscripcion;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para el Procesamiento, Reintento y Consulta de Pagos de Suscripciones SaaS (HU-23).
 */
@RestController
@RequestMapping("/api/v1/institutional/pagos")
@CrossOrigin(origins = "*")
public class ControladorPagoSuscripcion {

    private final ServicioPagoSuscripcion servicioPagoSuscripcion;

    public ControladorPagoSuscripcion(ServicioPagoSuscripcion servicioPagoSuscripcion) {
        this.servicioPagoSuscripcion = servicioPagoSuscripcion;
    }

    /**
     * Procesa el cobro de una orden de pedido.
     * Casos de uso: Procesar Pago <<include>> Emitir Notificación de Suscripción.
     */
    @PostMapping("/procesar")
    public ResponseEntity<PagoSuscripcionRespuesta> procesarPago(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody ProcesarPagoSolicitud solicitud) {
        PagoSuscripcionRespuesta respuesta = servicioPagoSuscripcion.procesarPago(authHeader, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Ejecuta el caso de uso extendido de reintento de pago sobre una orden rechazada.
     * Casos de uso: <<extend>> Reintentar Pago Fallido.
     */
    @PostMapping("/reintentar")
    public ResponseEntity<PagoSuscripcionRespuesta> reintentarPago(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody ProcesarPagoSolicitud solicitud) {
        PagoSuscripcionRespuesta respuesta = servicioPagoSuscripcion.reintentarPagoFallido(authHeader, solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Lista los intentos de pago asociados a una orden de pedido.
     */
    @GetMapping("/orden/{idOrden}")
    public ResponseEntity<List<Pago>> listarPagosDeOrden(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable UUID idOrden) {
        List<Pago> pagos = servicioPagoSuscripcion.listarPagosDeOrden(authHeader, idOrden);
        return ResponseEntity.ok(pagos);
    }
}
