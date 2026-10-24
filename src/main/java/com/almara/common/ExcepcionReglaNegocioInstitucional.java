package com.almara.common;

import org.springframework.http.HttpStatus;

/**
 * Excepción para violaciones de reglas de negocio del módulo institucional.
 */
public class ExcepcionReglaNegocioInstitucional extends RuntimeException {

    private final HttpStatus estado;
    private final String codigoError;
    private final java.util.Map<String, Object> detallesAdicionales;

    public ExcepcionReglaNegocioInstitucional(HttpStatus estado, String codigoError, String mensaje) {
        this(estado, codigoError, mensaje, null);
    }

    public ExcepcionReglaNegocioInstitucional(HttpStatus estado, String codigoError, String mensaje, java.util.Map<String, Object> detallesAdicionales) {
        super(mensaje);
        this.estado = estado != null ? estado : HttpStatus.BAD_REQUEST;
        this.codigoError = codigoError;
        this.detallesAdicionales = detallesAdicionales;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public java.util.Map<String, Object> getDetallesAdicionales() {
        return detallesAdicionales;
    }
}
