package com.almara.modules.emotion;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio emitido al registrar exitosamente una emoción.
 * Forma parte del patrón Publicador/Suscriptor (Pub/Sub) para desacoplar el procesamiento
 * del mapa de calor y análisis CEP de la respuesta HTTP inmediata (< 1000 ms).
 */
public class EventoEmocionRegistrada {

    private final UUID idEvento;
    private final TipoEmocion emocion;
    private final String tokenSesionTemporal;
    private final Instant fechaHoraEnvio;

    public EventoEmocionRegistrada(UUID idEvento, TipoEmocion emocion, String tokenSesionTemporal, Instant fechaHoraEnvio) {
        this.idEvento = idEvento;
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public TipoEmocion getEmocion() {
        return emocion;
    }

    public String getTokenSesionTemporal() {
        return tokenSesionTemporal;
    }

    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    @Override
    public String toString() {
        return "EventoEmocionRegistrada{" +
                "idEvento=" + idEvento +
                ", emocion=" + emocion +
                ", tokenSesionTemporal='" + tokenSesionTemporal + '\'' +
                ", fechaHoraEnvio=" + fechaHoraEnvio +
                '}';
    }

    public static class Constructor {
        private UUID idEvento;
        private TipoEmocion emocion;
        private String tokenSesionTemporal;
        private Instant fechaHoraEnvio;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor tokenSesionTemporal(String tokenSesionTemporal) {
            this.tokenSesionTemporal = tokenSesionTemporal;
            return this;
        }

        public Constructor fechaHoraEnvio(Instant fechaHoraEnvio) {
            this.fechaHoraEnvio = fechaHoraEnvio;
            return this;
        }

        public EventoEmocionRegistrada build() {
            return new EventoEmocionRegistrada(idEvento, emocion, tokenSesionTemporal, fechaHoraEnvio);
        }
    }
}
