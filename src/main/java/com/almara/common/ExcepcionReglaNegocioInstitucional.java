package com.almara.common;

import org.springframework.http.HttpStatus;

/**
 * Excepción para violaciones de reglas de negocio del módulo institucional.
 */
public class ExcepcionReglaNegocioInstitucional extends RuntimeException {

    private final HttpStatus estado;
    private final String codigoError;

    public ExcepcionReglaNegocioInstitucional(HttpStatus estado, String codigoError, String mensaje) {
        super(mensaje);
        this.estado = estado != null ? estado : HttpStatus.BAD_REQUEST;
        this.codigoError = codigoError;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigoError() {
        return codigoError;
    }
}
