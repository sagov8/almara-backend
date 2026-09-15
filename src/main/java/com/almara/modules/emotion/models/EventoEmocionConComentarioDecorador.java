package com.almara.modules.emotion.models;

import java.time.Instant;

/**
 * Decorador Concreto en el Patrón Decorator (GoF - Estructural) (HU-03).
 * Decora dinámicamente el evento base de emoción (EventoEmocion) agregando el atributo
 * commentContext y el texto sanitizado del comentario, sin modificar la estructura original
 * ni romper los contratos con microservicios que solo leen emoción o zona geográfica.
 */
public class EventoEmocionConComentarioDecorador extends EventoEmocionDecorador {

    private final String comentario;
    private final Instant fechaHoraComentario;

    public EventoEmocionConComentarioDecorador(EventoEmocion eventoDecorado, String comentario, Instant fechaHoraComentario) {
        super(eventoDecorado);
        this.comentario = comentario;
        this.fechaHoraComentario = fechaHoraComentario != null ? fechaHoraComentario : Instant.now();
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getFechaHoraComentario() {
        return fechaHoraComentario;
    }

    /**
     * Retorna el contexto específico del comentario (commentContext) tal como estipula la especificación de HU-03.
     */
    public String getCommentContext() {
        return comentario != null ? comentario : "";
    }

    @Override
    public String getContextoDescriptivo() {
        return String.format("%s [commentContext: \"%s\" @ %s]",
                super.getContextoDescriptivo(),
                comentario != null ? comentario : "",
                fechaHoraComentario);
    }
}
