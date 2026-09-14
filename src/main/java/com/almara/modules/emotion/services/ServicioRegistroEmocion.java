package com.almara.modules.emotion.services;

import com.almara.modules.emotion.models.EventoEmocionRegistrada;
import com.almara.modules.emotion.models.RegistroEmocionRespuesta;
import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import com.almara.modules.geo.services.ServicioResolucionZona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio encargado del registro de emociones ciudadanas (HU-01 y HU-04).
 * Cumple con Privacy by Design: convierte coordenadas GPS a celdas H3 y las descarta inmediatamente,
 * publicando el evento al bus interno para desacoplar el procesamiento y responder en < 1000 ms.
 */
@Service
public class ServicioRegistroEmocion {

    private static final Logger registroLog = LoggerFactory.getLogger(ServicioRegistroEmocion.class);

    private final ServicioControlFrecuencia servicioControlFrecuencia;
    private final ServicioResolucionZona servicioResolucionZona;
    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioRegistroEmocion(
            ServicioControlFrecuencia servicioControlFrecuencia,
            ServicioResolucionZona servicioResolucionZona,
            RepositorioReporteEmocion repositorioReporteEmocion,
            ApplicationEventPublisher publicadorEventos) {
        this.servicioControlFrecuencia = servicioControlFrecuencia;
        this.servicioResolucionZona = servicioResolucionZona;
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Procesa la solicitud de emoción ciudadana de acuerdo con HU-01 y HU-04:
     * 1. Valida control de frecuencia / rate limiting por token anónimo.
     * 2. Resuelve la celda H3 a partir de coordenadas GPS efímeras o del catálogo manual (HU-04).
     * 3. Genera identificador único y marca temporal oficial del servidor.
     * 4. Persiste el reporte de emoción anónimo con idCeldaH3 (sin coordenadas GPS).
     * 5. Publica evento asíncrono desacoplado para el cálculo colectivo posterior (Pub/Sub).
     * 6. Registra el bloqueo temporal en Redis/memoria.
     * 7. Retorna confirmación con idCeldaH3 y metadatos de respuesta.
     */
    public RegistroEmocionRespuesta registrarEmocion(RegistroEmocionSolicitud solicitud) {
        // 1. Control de frecuencia (Criterio 1: bloqueo temporal)
        servicioControlFrecuencia.validarDisponibilidad(solicitud.getTokenSesionTemporal());

        // 2. Resolución espacial a celda H3 (HU-04: Strategy + Adapter)
        CoordenadasGps coords = null;
        if (solicitud.tieneCoordenadasGps()) {
            coords = new CoordenadasGps(solicitud.getLatitud(), solicitud.getLongitud());
        }

        ResultadoResolucionZona zonaResuelta = servicioResolucionZona.resolverZona(
                coords,
                solicitud.getZonaManualId()
        );

        // 3. Metadatos oficiales de servidor (Criterio 3: sin PII, Criterio 4: timestamp automático)
        UUID idEvento = UUID.randomUUID();
        Instant fechaHoraEnvio = Instant.now();

        // 4. Persistencia anónima (HU-04: coordenadas exactas descartadas; se guarda celda_h3_id)
        ReporteEmocion reporte = ReporteEmocion.builder()
                .idEvento(idEvento)
                .idCeldaH3(zonaResuelta.getIdCeldaH3())
                .emocion(solicitud.getEmocion())
                .tokenSesionTemporal(solicitud.getTokenSesionTemporal())
                .fechaHora(fechaHoraEnvio)
                .build();

        repositorioReporteEmocion.guardar(reporte);

        // 5. Patrón Publicador/Suscriptor: emisión de evento desacoplada
        EventoEmocionRegistrada evento = EventoEmocionRegistrada.builder()
                .idEvento(idEvento)
                .emocion(solicitud.getEmocion())
                .tokenSesionTemporal(solicitud.getTokenSesionTemporal())
                .idCeldaH3(zonaResuelta.getIdCeldaH3())
                .zonaManualId(zonaResuelta.getZonaManualId())
                .fechaHoraEnvio(fechaHoraEnvio)
                .build();

        publicadorEventos.publishEvent(evento);
        registroLog.info("Evento de emoción publicado con éxito: [ID: {}, Emoción: {}, CeldaH3: {}]",
                idEvento, solicitud.getEmocion(), zonaResuelta.getIdCeldaH3());

        // 6. Aplicar bloqueo temporal
        servicioControlFrecuencia.registrarBloqueo(solicitud.getTokenSesionTemporal());

        // 7. Retornar confirmación (Criterio 5 de HU-01 y resultado de HU-04)
        return RegistroEmocionRespuesta.builder()
                .idEvento(idEvento)
                .mensaje("¡Tu emoción ha sido registrada con éxito! Gracias por contribuir al mapa emocional colectivo.")
                .emocionRegistrada(solicitud.getEmocion())
                .idCeldaH3(zonaResuelta.getIdCeldaH3())
                .nombreZona(zonaResuelta.getNombreZona())
                .fechaHoraEnvio(fechaHoraEnvio)
                .segundosBloqueo(servicioControlFrecuencia.obtenerSegundosBloqueo())
                .build();
    }
}
