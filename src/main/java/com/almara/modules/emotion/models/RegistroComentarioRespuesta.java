package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para la confirmación de registro de comentario (HU-03).
 */
public class RegistroComentarioRespuesta {

    private final UUID idEvento;
    private final String comentario;
    private final String mensaje;
    private final TipoEmocion emocion;
    private final String idCeldaH3;
    private final Instant fechaHoraEnvio;
    private final String commentContext;

    public RegistroComentarioRespuesta(UUID idEvento, String comentario, String mensaje,
                                      TipoEmocion emocion, String idCeldaH3,
                                      Instant fechaHoraEnvio, String commentContext) {
        this.idEvento = idEvento;
        this.comentario = comentario;
        this.mensaje = mensaje;
        this.emocion = emocion;
        this.idCeldaH3 = idCeldaH3;
        this.fechaHoraEnvio = fechaHoraEnvio;
        this.commentContext = commentContext;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public String getComentario() {
        return comentario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public TipoEmocion getEmocion() {
        return emocion;
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    public String getCommentContext() {
        return commentContext;
    }

    public static class Constructor {
        private UUID idEvento;
        private String comentario;
        private String mensaje;
        private TipoEmocion emocion;
        private String idCeldaH3;
        private Instant fechaHoraEnvio;
        private String commentContext;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor comentario(String comentario) {
            this.comentario = comentario;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor fechaHoraEnvio(Instant fechaHoraEnvio) {
            this.fechaHoraEnvio = fechaHoraEnvio;
            return this;
        }

        public Constructor commentContext(String commentContext) {
            this.commentContext = commentContext;
            return this;
        }

        public RegistroComentarioRespuesta build() {
            return new RegistroComentarioRespuesta(idEvento, comentario, mensaje, emocion, idCeldaH3, fechaHoraEnvio, commentContext);
        }
    }
}
