package com.almara.common;

/**
 * Excepción lanzada cuando un comentario complementario de emoción (HU-03)
 * viola las reglas de negocio (ej. contiene números, supera 200 caracteres,
 * contiene scripts o lenguaje ofensivo).
 */
public class ExcepcionComentarioInvalido extends RuntimeException {

    private final String codigoError;

    public ExcepcionComentarioInvalido(String codigoError, String mensaje) {
        super(mensaje);
        this.codigoError = codigoError;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
