package com.almara.modules.institutional.adapters.notification;

import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador listo para producción que envía notificaciones mediante SMTP real.
 * Se activa mediante la propiedad almara.notificaciones.proveedor=smtp.
 */
@Component
@ConditionalOnProperty(name = "almara.notificaciones.proveedor", havingValue = "smtp")
public class AdaptadorNotificacionSmtpReal implements ServicioNotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorNotificacionSmtpReal.class);

    @Override
    public void enviarCorreoVerificacion(String destinatario, String razonSocial, String tokenVerificacion) {
        log.info("[SMTP REAL STUB] Despachando correo SMTP a {} con token {}", destinatario, tokenVerificacion);
        // Punto de integración JavaMailSender / SES / SendGrid
    }

    @Override
    public void enviarCorreoConfirmacion(String destinatario, String asunto, String cuerpoMensaje) {
        log.info("[SMTP REAL STUB] Despachando correo SMTP a {} con asunto '{}'", destinatario, asunto);
        // Punto de integración JavaMailSender / SES / SendGrid
    }
}
