package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio emitido al asociar e indicar exitosamente la intensidad de una emoción (HU-02).
 * Forma parte del patrón Publicador/Suscriptor (Pub/Sub) para desacoplar el cálculo de calor
 * y tendencias por intensidad del tiempo de respuesta inmediato al usuario (< 300 ms).
 */
public class EventoIntensidadRegistrada {

    private final UUID idEvento;
    private final TipoEmocion emocion;
    private final Integer nivelIntensidad;
    private final String idCeldaH3;
    private final Instant fechaHora;

    public EventoIntensidadRegistrada(UUID idEvento, TipoEmocion emocion, Integer nivelIntensidad,
                                      String idCeldaH3, Instant fechaHora) {
        this.idEvento = idEvento;
        this.emocion = emocion;
        this.nivelIntensidad = nivelIntensidad;
        this.idCeldaH3 = idCeldaH3;
        this.fechaHora = fechaHora;
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

    public Integer getNivelIntensidad() {
        return nivelIntensidad;
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    @Override
    public String toString() {
        return "EventoIntensidadRegistrada{" +
                "idEvento=" + idEvento +
                ", emocion=" + emocion +
                ", nivelIntensidad=" + nivelIntensidad +
                ", idCeldaH3='" + idCeldaH3 + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }

    public static class Constructor {
        private UUID idEvento;
        private TipoEmocion emocion;
        private Integer nivelIntensidad;
        private String idCeldaH3;
        private Instant fechaHora;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor nivelIntensidad(Integer nivelIntensidad) {
            this.nivelIntensidad = nivelIntensidad;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor fechaHora(Instant fechaHora) {
            this.fechaHora = fechaHora;
            return this;
        }

        public EventoIntensidadRegistrada build() {
            return new EventoIntensidadRegistrada(idEvento, emocion, nivelIntensidad, idCeldaH3, fechaHora);
        }
    }
}
