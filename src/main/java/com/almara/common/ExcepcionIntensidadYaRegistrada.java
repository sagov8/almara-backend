package com.almara.common;

import java.util.UUID;

/**
 * Excepción de negocio que garantiza la inmutabilidad de la transacción (HU-02).
 * Lanzada si se intenta modificar, corregir o sobreescribir el nivel de intensidad
 * de un evento de emoción que ya fue previamente confirmado y registrado.
 */
public class ExcepcionIntensidadYaRegistrada extends RuntimeException {

    private final UUID idEvento;

    public ExcepcionIntensidadYaRegistrada(UUID idEvento) {
        super("El evento de emoción con ID " + idEvento + " ya cuenta con una intensidad registrada y es inmutable.");
        this.idEvento = idEvento;
    }

    public UUID getIdEvento() {
        return idEvento;
    }
}
