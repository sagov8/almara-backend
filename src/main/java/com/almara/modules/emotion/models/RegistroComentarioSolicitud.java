package com.almara.modules.emotion.models;

import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTO para la solicitud de registro o actualización de un comentario complementario (HU-03).
 */
public class RegistroComentarioSolicitud {

    private UUID idEvento;

    private String comentario;

    public RegistroComentarioSolicitud() {
    }

    public RegistroComentarioSolicitud(UUID idEvento, String comentario) {
        this.idEvento = idEvento;
        this.comentario = comentario;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(UUID idEvento) {
        this.idEvento = idEvento;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public static class Constructor {
        private UUID idEvento;
        private String comentario;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor comentario(String comentario) {
            this.comentario = comentario;
            return this;
        }

        public RegistroComentarioSolicitud build() {
            return new RegistroComentarioSolicitud(idEvento, comentario);
        }
    }
}
