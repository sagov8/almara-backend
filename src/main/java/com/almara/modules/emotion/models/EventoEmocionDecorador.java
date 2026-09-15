package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Decorador Abstracto en el Patrón Decorator (GoF - Estructural) (HU-03).
 * Mantiene la referencia a la instancia envuelta de EventoEmocion y delega
 * la implementación predeterminada de todos los métodos del contrato.
 */
public abstract class EventoEmocionDecorador implements EventoEmocion {

    protected final EventoEmocion eventoDecorado;

    public EventoEmocionDecorador(EventoEmocion eventoDecorado) {
        if (eventoDecorado == null) {
            throw new IllegalArgumentException("El evento de emoción a decorar no puede ser nulo");
        }
        this.eventoDecorado = eventoDecorado;
    }

    @Override
    public UUID getIdEvento() {
        return eventoDecorado.getIdEvento();
    }

    @Override
    public TipoEmocion getEmocion() {
        return eventoDecorado.getEmocion();
    }

    @Override
    public String getTokenSesionTemporal() {
        return eventoDecorado.getTokenSesionTemporal();
    }

    @Override
    public String getIdCeldaH3() {
        return eventoDecorado.getIdCeldaH3();
    }

    @Override
    public String getZonaManualId() {
        return eventoDecorado.getZonaManualId();
    }

    @Override
    public Instant getFechaHoraEnvio() {
        return eventoDecorado.getFechaHoraEnvio();
    }

    @Override
    public String getContextoDescriptivo() {
        return eventoDecorado.getContextoDescriptivo();
    }
}
