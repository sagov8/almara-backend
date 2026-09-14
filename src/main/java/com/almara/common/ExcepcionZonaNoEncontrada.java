package com.almara.common;

/**
 * Excepción de negocio lanzada cuando el identificador de zona manual especificado
 * no existe dentro del catálogo predefinido.
 */
public class ExcepcionZonaNoEncontrada extends RuntimeException {

    public ExcepcionZonaNoEncontrada(String mensaje) {
        super(mensaje);
    }
}
