package com.almara.common;

/**
 * Excepción lanzada cuando un usuario o sesión intenta registrar una emoción
 * antes de que concluya el período de espera obligatorio (lapso de bloqueo).
 */
public class ExcepcionSolicitudesExcesivas extends RuntimeException {

    private final long segundosRestantes;

    public ExcepcionSolicitudesExcesivas(String mensaje, long segundosRestantes) {
        super(mensaje);
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }
}
