package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) para la respuesta de confirmación de registro de emoción (HU-01 y HU-04).
 * Informa el éxito, la celda H3 asignada (sin exponer coordenadas exactas), la marca temporal y el cooldown.
 */
public class RegistroEmocionRespuesta {

    private UUID idEvento;
    private String mensaje;
    private TipoEmocion emocionRegistrada;
    private String idCeldaH3;
    private String nombreZona;
    private Instant fechaHoraEnvio;
    private long segundosBloqueo;

    public RegistroEmocionRespuesta() {
    }

    public RegistroEmocionRespuesta(UUID idEvento, String mensaje, TipoEmocion emocionRegistrada,
                                   String idCeldaH3, String nombreZona,
                                   Instant fechaHoraEnvio, long segundosBloqueo) {
        this.idEvento = idEvento;
        this.mensaje = mensaje;
        this.emocionRegistrada = emocionRegistrada;
        this.idCeldaH3 = idCeldaH3;
        this.nombreZona = nombreZona;
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

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
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
        private String idCeldaH3;
        private String nombreZona;
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

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor nombreZona(String nombreZona) {
            this.nombreZona = nombreZona;
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
            return new RegistroEmocionRespuesta(idEvento, mensaje, emocionRegistrada, idCeldaH3, nombreZona, fechaHoraEnvio, segundosBloqueo);
        }
    }
}
