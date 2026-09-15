package com.almara.modules.emotion.services;

import com.almara.common.ExcepcionIntensidadYaRegistrada;
import com.almara.common.ExcepcionReporteNoEncontrado;
import com.almara.modules.emotion.models.EventoIntensidadRegistrada;
import com.almara.modules.emotion.models.RegistroIntensidadRespuesta;
import com.almara.modules.emotion.models.RegistroIntensidadSolicitud;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio de lógica de negocio para la indicación de intensidad de emociones (HU-02).
 * Vincula la intensidad seleccionada al evento original generado en HU-01.
 * Asegura inmutabilidad (Criterio y Limitación de alcance), anonimato (RNF Seguridad)
 * y procesamiento en menos de 300 ms (RNF Rendimiento).
 */
@Service
public class ServicioRegistroIntensidad {

    private static final Logger log = LoggerFactory.getLogger(ServicioRegistroIntensidad.class);

    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final ApplicationEventPublisher publicadorEventos;

    public ServicioRegistroIntensidad(
            RepositorioReporteEmocion repositorioReporteEmocion,
            ApplicationEventPublisher publicadorEventos) {
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.publicadorEventos = publicadorEventos;
    }

    /**
     * Procesa y asocia el nivel de intensidad al reporte de emoción correspondiente:
     * 1. Busca el reporte previamente registrado en HU-01.
     * 2. Si no existe, arroja ExcepcionReporteNoEncontrado (HTTP 404).
     * 3. Valida la inmutabilidad: si ya posee una intensidad registrada, arroja ExcepcionIntensidadYaRegistrada (HTTP 409).
     * 4. Asigna la intensidad (escala 1 a 5) y persiste el reporte actualizado.
     * 5. Emite de forma desacoplada el evento de dominio EventoIntensidadRegistrada (Pub/Sub).
     * 6. Retorna DTO de confirmación en tiempo inferior a 300 ms.
     */
    public RegistroIntensidadRespuesta registrarIntensidad(RegistroIntensidadSolicitud solicitud) {
        if (solicitud.getIdEvento() == null) {
            throw new ExcepcionReporteNoEncontrado("El identificador de evento de emoción (idEvento) generado en HU-01 es obligatorio");
        }

        // 1. Verificar existencia del reporte generado en HU-01
        ReporteEmocion reporte = repositorioReporteEmocion.buscarPorId(solicitud.getIdEvento())
                .orElseThrow(() -> new ExcepcionReporteNoEncontrado(solicitud.getIdEvento()));

        // 2. Control de inmutabilidad (HU-02 Limitaciones del Alcance)
        if (reporte.tieneIntensidad()) {
            throw new ExcepcionIntensidadYaRegistrada(solicitud.getIdEvento());
        }

        // 3. Persistencia de la intensidad
        reporte.setIntensidad(solicitud.getNivelIntensidad());
        repositorioReporteEmocion.guardar(reporte);

        Instant fechaHoraActual = Instant.now();
        String etiqueta = obtenerEtiquetaIntensidad(solicitud.getNivelIntensidad());

        // 4. Emisión asíncrona del evento de dominio desacoplado (Patrón Publicador/Suscriptor)
        EventoIntensidadRegistrada evento = EventoIntensidadRegistrada.builder()
                .idEvento(reporte.getIdEvento())
                .emocion(reporte.getEmocion())
                .nivelIntensidad(solicitud.getNivelIntensidad())
                .idCeldaH3(reporte.getIdCeldaH3())
                .fechaHora(fechaHoraActual)
                .build();

        publicadorEventos.publishEvent(evento);

        log.info("Intensidad registrada con éxito para evento [ID: {}, Nivel: {} ({}), CeldaH3: {}]",
                reporte.getIdEvento(), solicitud.getNivelIntensidad(), etiqueta, reporte.getIdCeldaH3());

        // 5. Retornar DTO de confirmación
        return RegistroIntensidadRespuesta.builder()
                .idEvento(reporte.getIdEvento())
                .nivelIntensidad(solicitud.getNivelIntensidad())
                .etiquetaIntensidad(etiqueta)
                .emocion(reporte.getEmocion())
                .idCeldaH3(reporte.getIdCeldaH3())
                .mensaje("¡Intensidad de la emoción registrada con éxito! Tu aporte ha sido integrado al mapa comunitario.")
                .fechaHoraEnvio(fechaHoraActual)
                .build();
    }

    /**
     * Mapea el nivel entero (1-5) a la etiqueta visual estipulada en el mockup de la HU-02.
     */
    public String obtenerEtiquetaIntensidad(int nivel) {
        return switch (nivel) {
            case 1 -> "Leve";
            case 2 -> "Leve-Moderado";
            case 3 -> "Moderado";
            case 4 -> "Moderado-Intenso";
            case 5 -> "Intenso";
            default -> "Nivel " + nivel;
        };
    }
}
