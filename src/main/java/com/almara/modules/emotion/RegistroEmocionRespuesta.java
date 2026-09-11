package com.almara.modules.emotion;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) para la respuesta de confirmación de registro de emoción.
 * Informa el éxito, la marca temporal del servidor y el tiempo de bloqueo (cooldown).
 */
public class RegistroEmocionRespuesta {

    private UUID idEvento;
    private String mensaje;
    private TipoEmocion emocionRegistrada;
    private Instant fechaHoraEnvio;
    private long segundosBloqueo;

    public RegistroEmocionRespuesta() {
    }

    public RegistroEmocionRespuesta(UUID idEvento, String mensaje, TipoEmocion emocionRegistrada, Instant fechaHoraEnvio, long segundosBloqueo) {
        this.idEvento = idEvento;
        this.mensaje = mensaje;
        this.emocionRegistrada = emocionRegistrada;
        this.fechaHoraEnvio = fechaHoraEnvio;
        this.segundosBloqueo = segundosBloqueo;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public TipoEmocion getEmocionRegistrada() {
        return emocionRegistrada;
    }

    public void setEmocionRegistrada(TipoEmocion emocionRegistrada) {
        this.emocionRegistrada = emocionRegistrada;
    }

    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    public void setFechaHoraEnvio(Instant fechaHoraEnvio) {
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    public long getSegundosBloqueo() {
        return segundosBloqueo;
    }

    public void setSegundosBloqueo(long segundosBloqueo) {
        this.segundosBloqueo = segundosBloqueo;
    }

    public static class Constructor {
        private UUID idEvento;
        private String mensaje;
        private TipoEmocion emocionRegistrada;
        private Instant fechaHoraEnvio;
        private long segundosBloqueo;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Constructor emocionRegistrada(TipoEmocion emocionRegistrada) {
            this.emocionRegistrada = emocionRegistrada;
            return this;
        }

        public Constructor fechaHoraEnvio(Instant fechaHoraEnvio) {
            this.fechaHoraEnvio = fechaHoraEnvio;
            return this;
        }

        public Constructor segundosBloqueo(long segundosBloqueo) {
            this.segundosBloqueo = segundosBloqueo;
            return this;
        }

        public RegistroEmocionRespuesta build() {
            return new RegistroEmocionRespuesta(idEvento, mensaje, emocionRegistrada, fechaHoraEnvio, segundosBloqueo);
        }
    }
}
