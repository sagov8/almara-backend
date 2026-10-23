package com.almara.modules.institutional.adapters.notification;

import com.almara.modules.institutional.ports.ServicioNotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptador simulado determinista para el despacho de correos en entorno local y de pruebas.
 */
@Component
@Primary
@ConditionalOnProperty(name = "almara.notificaciones.proveedor", havingValue = "simulado", matchIfMissing = true)
public class AdaptadorNotificacionSimulada implements ServicioNotificacionPort {

    private static final Logger log = LoggerFactory.getLogger(AdaptadorNotificacionSimulada.class);

    private final List<NotificacionEmitida> notificaciones = new CopyOnWriteArrayList<>();

    @Override
    public void enviarCorreoVerificacion(String destinatario, String razonSocial, String tokenVerificacion) {
        String asunto = "Almara — Verificación de Cuenta Institucional: " + razonSocial;
        String cuerpo = "Estimado representante de " + razonSocial + ",\n\n"
                + "Su registro corporativo ha sido recibido con éxito.\n"
                + "Para activar su cuenta y habilitar su plan institucional, ingrese el siguiente token de verificación:\n\n"
                + "Token: " + tokenVerificacion + "\n\n"
                + "Equipo de Seguridad Almara.";

        NotificacionEmitida notificacion = new NotificacionEmitida(destinatario, asunto, cuerpo, tokenVerificacion, Instant.now());
        notificaciones.add(notificacion);

        log.info("[SIMULADOR NOTIFICACION] Correo de verificación enviado a {}: Token=[{}]", destinatario, tokenVerificacion);
    }

    @Override
    public void enviarCorreoConfirmacion(String destinatario, String asunto, String cuerpoMensaje) {
        NotificacionEmitida notificacion = new NotificacionEmitida(destinatario, asunto, cuerpoMensaje, null, Instant.now());
        notificaciones.add(notificacion);

        log.info("[SIMULADOR NOTIFICACION] Correo enviado a {}: Asunto='{}'", destinatario, asunto);
    }

    public List<NotificacionEmitida> obtenerNotificaciones() {
        return notificaciones;
    }

    public Optional<NotificacionEmitida> obtenerUltimaNotificacionPorDestinatario(String correo) {
        if (correo == null) {
            return Optional.empty();
        }
        for (int i = notificaciones.size() - 1; i >= 0; i--) {
            NotificacionEmitida n = notificaciones.get(i);
            if (correo.equalsIgnoreCase(n.destinatario())) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    public void limpiar() {
        notificaciones.clear();
    }

    public record NotificacionEmitida(String destinatario, String asunto, String cuerpo, String tokenVerificacion, Instant fecha) {
    }
}
