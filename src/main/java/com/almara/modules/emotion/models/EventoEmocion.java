package com.almara.modules.emotion.models;

import java.time.Instant;
import java.util.UUID;

/**
 * Componente Base del Patrón Decorator (GoF - Estructural) para eventos de emoción (HU-03).
 * Define el contrato inmutable consumido por microservicios, buses de eventos y módulos
 * que solo requieren los datos esenciales de la emoción y ubicación geográfica anónima.
 */
public interface EventoEmocion {

    UUID getIdEvento();

    TipoEmocion getEmocion();

    String getTokenSesionTemporal();

    String getIdCeldaH3();

    String getZonaManualId();

    Instant getFechaHoraEnvio();

    /**
     * Retorna la representación o contexto textual enriquecido del evento.
     * En el evento base devuelve únicamente la emoción y zona;
     * el decorador de comentario añade dinámicamente el atributo commentContext.
     */
    String getContextoDescriptivo();
}
