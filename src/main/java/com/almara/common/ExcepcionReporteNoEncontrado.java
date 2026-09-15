package com.almara.common;

import java.util.UUID;

/**
 * Excepción de negocio lanzada cuando el evento de emoción al que se pretende
 * asociar una intensidad (HU-02) o comentario (HU-03) no existe en el sistema.
 */
public class ExcepcionReporteNoEncontrado extends RuntimeException {

    private final UUID idEvento;

    public ExcepcionReporteNoEncontrado(UUID idEvento) {
        super("No se encontró ningún reporte de emoción registrado con el identificador: " + idEvento);
        this.idEvento = idEvento;
    }

    public ExcepcionReporteNoEncontrado(String mensaje) {
        super(mensaje);
        this.idEvento = null;
    }

    public UUID getIdEvento() {
        return idEvento;
    }
}
