package com.almara.modules.emotion.services;

import com.almara.common.ExcepcionReporteNoEncontrado;
import com.almara.modules.emotion.models.*;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Servicio de aplicación para el registro y gestión de comentarios complementarios (HU-03).
 * Aplica el Patrón Decorator sobre EventoEmocion para enriquecer el evento sin romper
 * contratos existentes, asegurando validación estricta y respuesta < 300 ms.
 */
@Service
public class ServicioRegistroComentario {

    private static final Logger registroLogs = LoggerFactory.getLogger(ServicioRegistroComentario.class);

    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final ServicioModeracionComentario servicioModeracionComentario;

    public ServicioRegistroComentario(
            RepositorioReporteEmocion repositorioReporteEmocion,
            ServicioModeracionComentario servicioModeracionComentario) {
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.servicioModeracionComentario = servicioModeracionComentario;
    }

    /**
     * Registra o actualiza el comentario complementario asociado a un reporte de emoción existente.
     *
     * @param solicitud DTO con idEvento y comentario opcional.
     * @return Respuesta estructurada con confirmación y contexto del evento decorado.
     */
    public RegistroComentarioRespuesta registrarComentario(RegistroComentarioSolicitud solicitud) {
        if (solicitud.getIdEvento() == null) {
            throw new IllegalArgumentException("El identificador de evento (idEvento) es obligatorio");
        }

        UUID idEvento = solicitud.getIdEvento();

        // Verificar existencia del reporte previo (HU-01)
        ReporteEmocion reporte = repositorioReporteEmocion.buscarPorId(idEvento)
                .orElseThrow(() -> new ExcepcionReporteNoEncontrado(idEvento));

        // Validación, sanitización y moderación del comentario (HU-03 Criterios 1, 2, 3)
        String comentarioLimpio = servicioModeracionComentario.procesarYValidarComentario(solicitud.getComentario());

        // Actualizar entidad de dominio y persistir
        reporte.setComentario(comentarioLimpio);
        repositorioReporteEmocion.guardar(reporte);

        // Patrón Decorator: instanciar el evento base y decorarlo dinámicamente con commentContext
        EventoEmocion eventoBase = new EventoEmocionBase(
                reporte.getIdEvento(),
                reporte.getEmocion(),
                reporte.getTokenSesionTemporal(),
                reporte.getIdCeldaH3(),
                null,
                reporte.getFechaHora()
        );

        EventoEmocion eventoDecorado = eventoBase;
        String commentContext = "";
        if (comentarioLimpio != null) {
            EventoEmocionConComentarioDecorador decorador = new EventoEmocionConComentarioDecorador(
                    eventoBase,
                    comentarioLimpio,
                    Instant.now()
            );
            eventoDecorado = decorador;
            commentContext = decorador.getCommentContext();
            registroLogs.info("Evento decorado con comentario: [ID: {}, Contexto: {}]", idEvento, decorador.getContextoDescriptivo());
        } else {
            registroLogs.info("Evento procesado sin comentario opcional: [ID: {}]", idEvento);
        }

        String mensaje = comentarioLimpio != null
                ? "Comentario complementario registrado con éxito."
                : "Comentario omitido; reporte procesado normalmente.";

        return RegistroComentarioRespuesta.builder()
                .idEvento(reporte.getIdEvento())
                .comentario(comentarioLimpio)
                .mensaje(mensaje)
                .emocion(reporte.getEmocion())
                .idCeldaH3(reporte.getIdCeldaH3())
                .fechaHoraEnvio(reporte.getFechaHora())
                .commentContext(commentContext)
                .build();
    }
}
