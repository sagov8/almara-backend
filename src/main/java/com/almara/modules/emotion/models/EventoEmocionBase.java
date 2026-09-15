package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Componente Concreto en el Patrón Decorator (HU-03).
 * Representa el evento de emoción estándar (HU-01 y HU-04) sin atributos adicionales.
 */
public class EventoEmocionBase implements EventoEmocion {

    private final UUID idEvento;
    private final TipoEmocion emocion;
    private final String tokenSesionTemporal;
    private final String idCeldaH3;
    private final String zonaManualId;
    private final Instant fechaHoraEnvio;

    public EventoEmocionBase(UUID idEvento, TipoEmocion emocion, String tokenSesionTemporal,
                             String idCeldaH3, String zonaManualId, Instant fechaHoraEnvio) {
        this.idEvento = idEvento;
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
        this.idCeldaH3 = idCeldaH3;
        this.zonaManualId = zonaManualId;
        this.fechaHoraEnvio = fechaHoraEnvio;
    }

    public static EventoEmocionBase desde(EventoEmocionRegistrada evento) {
        return new EventoEmocionBase(
                evento.getIdEvento(),
                evento.getEmocion(),
                evento.getTokenSesionTemporal(),
                evento.getIdCeldaH3(),
                evento.getZonaManualId(),
                evento.getFechaHoraEnvio()
        );
    }

    @Override
    public UUID getIdEvento() {
        return idEvento;
    }

    @Override
    public TipoEmocion getEmocion() {
        return emocion;
    }

    @Override
    public String getTokenSesionTemporal() {
        return tokenSesionTemporal;
    }

    @Override
    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    @Override
    public String getZonaManualId() {
        return zonaManualId;
    }

    @Override
    public Instant getFechaHoraEnvio() {
        return fechaHoraEnvio;
    }

    @Override
    public String getContextoDescriptivo() {
        return String.format("[Evento: %s | Emocion: %s | Celda: %s]", idEvento, emocion, idCeldaH3);
    }
}
