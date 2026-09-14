package com.almara.common;

/**
 * Excepción de negocio lanzada cuando no se suministran coordenadas GPS temporales
 * ni un identificador de zona manual válido (Criterio 4 de HU-04).
 */
public class ExcepcionUbicacionRequerida extends RuntimeException {

    public ExcepcionUbicacionRequerida(String mensaje) {
        super(mensaje);
    }
}
