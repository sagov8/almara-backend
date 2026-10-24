package com.almara.modules.institutional.ports;

import com.almara.modules.institutional.models.OrdenPedido;
import com.almara.modules.institutional.models.Pago;

/**
 * Puerto de dominio para la integración con pasarelas de pago externas (UML / Arquitectura Hexagonal).
 */
public interface PasarelaPagoPort {

    /**
     * Procesa el cobro de una orden de pedido utilizando el medio y token especificados.
     */
    Pago procesarCobro(OrdenPedido orden, String medioPago, String tokenMedioPago);

    /**
     * Retorna el nombre identificador del proveedor de pagos activo.
     */
    String obtenerNombreProveedor();
}
