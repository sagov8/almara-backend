package com.almara.modules.emotion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio encargado del registro de emociones ciudadanas.
 * Implementa el patrón Publicador/Suscriptor publicando el evento al bus interno
 * para desacoplar el procesamiento pesado y responder en menos de 1000 ms.
 */
@Service
public class ServicioRegistroEmocion {

    private static final Logger registroLog = LoggerFactory.getLogger(ServicioRegistroEmocion.class);

    private final ServicioControlFrecuencia servicioControlFrecuencia;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioRegistroEmocion(
            ServicioControlFrecuencia servicioControlFrecuencia,
            ApplicationEventPublisher publicadorEventos) {
        this.servicioControlFrecuencia = servicioControlFrecuencia;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Procesa la solicitud de emoción ciudadana de acuerdo con los criterios de HU-01:
     * 1. Verifica lapso de bloqueo / rate limiting por token anónimo.
     * 2. Genera identificador único y marca temporal oficial del servidor.
     * 3. Publica evento asíncrono para el cálculo colectivo posterior.
     * 4. Registra el bloqueo temporal para evitar envíos repetitivos inmediatos.
     * 5. Retorna respuesta de confirmación.
     */
    public RegistroEmocionRespuesta registrarEmocion(RegistroEmocionSolicitud solicitud) {
        // 1. Control de frecuencia (Criterio 1: bloqueo temporal)
        servicioControlFrecuencia.validarDisponibilidad(solicitud.getTokenSesionTemporal());

        // 2. Metadatos de servidor (Criterio 3: sin PII, Criterio 4: timestamp automático)
        UUID idEvento = UUID.randomUUID();
        Instant fechaHoraEnvio = Instant.now();

        // 3. Patrón Publicador/Suscriptor: emisión de evento desacoplada
        EventoEmocionRegistrada evento = EventoEmocionRegistrada.builder()
                .idEvento(idEvento)
                .emocion(solicitud.getEmocion())
                .tokenSesionTemporal(solicitud.getTokenSesionTemporal())
                .fechaHoraEnvio(fechaHoraEnvio)
                .build();

        publicadorEventos.publishEvent(evento);
        registroLog.info("Evento de emoción publicado con éxito: [ID: {}, Emoción: {}]", idEvento, solicitud.getEmocion());

        // 4. Aplicar bloqueo temporal
        servicioControlFrecuencia.registrarBloqueo(solicitud.getTokenSesionTemporal());

        // 5. Retornar confirmación (Criterio 5: mensaje de confirmación)
        return RegistroEmocionRespuesta.builder()
                .idEvento(idEvento)
                .mensaje("¡Tu emoción ha sido registrada con éxito! Gracias por contribuir al mapa emocional colectivo.")
                .emocionRegistrada(solicitud.getEmocion())
                .fechaHoraEnvio(fechaHoraEnvio)
                .segundosBloqueo(servicioControlFrecuencia.obtenerSegundosBloqueo())
                .build();
    }
}
