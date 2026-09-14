package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio emitido al registrar exitosamente una emoción ciudadana (HU-01 y HU-04).
 * Forma parte del patrón Publicador/Suscriptor (Pub/Sub) para desacoplar el procesamiento
 * del mapa de calor y análisis CEP de la respuesta HTTP inmediata (< 1000 ms).
 * Cumple con RNF Seguridad: No incluye coordenadas precisas, solo el identificador de celda H3 anónima.
 */
public class EventoEmocionRegistrada {

    private final UUID idEvento;
    private final TipoEmocion emocion;
    private final String tokenSesionTemporal;
    private final String idCeldaH3;
    private final String zonaManualId;
    private final Instant fechaHoraEnvio;

    public EventoEmocionRegistrada(UUID idEvento, TipoEmocion emocion, String tokenSesionTemporal,
                                  String idCeldaH3, String zonaManualId, Instant fechaHoraEnvio) {
        this.idEvento = idEvento;
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
        this.idCeldaH3 = idCeldaH3;
        this.zonaManualId = zonaManualId;
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

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    @Override
    public String toString() {
        return "EventoEmocionRegistrada{" +
                "idEvento=" + idEvento +
                ", emocion=" + emocion +
                ", idCeldaH3='" + idCeldaH3 + '\'' +
                ", zonaManualId='" + zonaManualId + '\'' +
                ", fechaHoraEnvio=" + fechaHoraEnvio +
                '}';
    }

    public static class Constructor {
        private UUID idEvento;
        private TipoEmocion emocion;
        private String tokenSesionTemporal;
        private String idCeldaH3;
        private String zonaManualId;
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

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor fechaHoraEnvio(Instant fechaHoraEnvio) {
            this.fechaHoraEnvio = fechaHoraEnvio;
            return this;
        }

        public EventoEmocionRegistrada build() {
            return new EventoEmocionRegistrada(idEvento, emocion, tokenSesionTemporal, idCeldaH3, zonaManualId, fechaHoraEnvio);
        }
    }
}
