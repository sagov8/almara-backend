package com.almara.modules.institutional.adapters.payment;

import com.almara.modules.institutional.models.OrdenPedido;
import com.almara.modules.institutional.models.Pago;
import com.almara.modules.institutional.ports.PasarelaPagoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de dominio simulado para procesamiento determinista de pagos SaaS (HU-23).
 * Activo por defecto en pruebas y entornos de desarrollo sin credenciales de pasarela externa.
 */
@Component
@ConditionalOnProperty(name = "almara.pagos.proveedor", havingValue = "simulado", matchIfMissing = true)
public class AdaptadorPasarelaPagoSimulada implements PasarelaPagoPort {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorPasarelaPagoSimulada.class);

    @Override
    public Pago procesarCobro(OrdenPedido orden, String medioPago, String tokenMedioPago) {
        String tokenUpper = tokenMedioPago != null ? tokenMedioPago.toUpperCase() : "";
        String medioUpper = medioPago != null ? medioPago.toUpperCase() : "";

        // Patrones oficiales de simulación de rechazo:
        boolean simularRechazo = tokenUpper.contains("RECHAZAR")
                || tokenUpper.contains("FALLO")
                || tokenUpper.contains("DECLINED")
                || tokenUpper.contains("FAIL")
                || tokenUpper.endsWith("0002")
                || medioUpper.contains("RECHAZAR");

        UUID idPago = UUID.randomUUID();
        String referenciaExterna = "PAY-SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (simularRechazo) {
            log.warn("[PASARELA SIMULADA] Pago RECHAZADO para orden {}. Token={}", orden.getNumeroOrden(), tokenMedioPago);
            return Pago.builder()
                    .idPago(idPago)
                    .idOrden(orden.getIdOrden())
                    .referenciaExterna(referenciaExterna)
                    .montoPagado(orden.getMontoTotal())
                    .metodoPago(medioPago != null ? medioPago : "TARJETA_SIMULADA")
                    .estadoPago("RECHAZADO")
                    .motivoRechazo("Fondos insuficientes o tarjeta declinada por el banco emisor")
                    .fechaTransaccion(Instant.now())
                    .build();
        }

        log.info("[PASARELA SIMULADA] Pago APROBADO para orden {} por ${}. Ref={}",
                orden.getNumeroOrden(), orden.getMontoTotal(), referenciaExterna);

        return Pago.builder()
                .idPago(idPago)
                .idOrden(orden.getIdOrden())
                .referenciaExterna(referenciaExterna)
                .montoPagado(orden.getMontoTotal())
                .metodoPago(medioPago != null ? medioPago : "TARJETA_SIMULADA")
                .estadoPago("APROBADO")
                .motivoRechazo(null)
                .fechaTransaccion(Instant.now())
                .build();
    }

    @Override
    public String obtenerNombreProveedor() {
        return "SIMULADOR_DETERMINISTA";
    }
}
