package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.AdquirirPlanSolicitud;
import com.almara.modules.institutional.dto.OrdenSuscripcionRespuesta;
import com.almara.modules.institutional.services.ServicioCheckoutSuscripcion;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controlador REST para el Checkout y Generación de Órdenes de Suscripción (HU-22).
 */
@RestController
@RequestMapping("/api/v1/institutional/checkout")
@CrossOrigin(origins = "*")
public class ControladorCheckoutSuscripcion {

    private final ServicioCheckoutSuscripcion servicioCheckoutSuscripcion;

    public ControladorCheckoutSuscripcion(ServicioCheckoutSuscripcion servicioCheckoutSuscripcion) {
        this.servicioCheckoutSuscripcion = servicioCheckoutSuscripcion;
    }

    /**
     * Endpoint para adquirir un plan de suscripción en el carrito y generar la orden de pedido.
     * Casos de uso: Adquirir Plan de Suscripción <<include>> Generar Orden de Suscripción.
     */
    @PostMapping("/orden")
    public ResponseEntity<OrdenSuscripcionRespuesta> adquirirPlanYGenerarOrden(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody AdquirirPlanSolicitud solicitud) {
        OrdenSuscripcionRespuesta respuesta = servicioCheckoutSuscripcion.seleccionarPlanEnCarritoYGenerarOrden(authHeader, solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Endpoint para consultar los detalles de una orden de suscripción por su ID.
     */
    @GetMapping("/ordenes/{idOrden}")
    public ResponseEntity<OrdenSuscripcionRespuesta> consultarOrdenPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable UUID idOrden) {
        OrdenSuscripcionRespuesta respuesta = servicioCheckoutSuscripcion.consultarOrdenPorId(authHeader, idOrden);
        return ResponseEntity.ok(respuesta);
    }
}
