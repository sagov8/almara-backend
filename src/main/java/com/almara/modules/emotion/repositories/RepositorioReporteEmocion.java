package com.almara.modules.emotion.repositories;

import com.almara.modules.emotion.models.ReporteEmocion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia para los reportes individuales anónimos de emoción (Patrón Repository).
 */
public interface RepositorioReporteEmocion {

    /**
     * Persiste un reporte de emoción.
     */
    ReporteEmocion guardar(ReporteEmocion reporte);

    /**
     * Busca un reporte por su identificador de evento único.
     */
    Optional<ReporteEmocion> buscarPorId(UUID idEvento);

    /**
     * Retorna todos los reportes asociados a una celda H3 específica.
     */
    List<ReporteEmocion> buscarPorCeldaH3(String idCeldaH3);

    /**
     * Cuenta el total de reportes registrados en el sistema.
     */
    long contarTotal();

    /**
     * Retorna todos los reportes de emoción registrados en el sistema.
     */
    List<ReporteEmocion> obtenerTodos();

    /**
     * Limpia los reportes almacenados (de utilidad para pruebas automatizadas).
     */
    void limpiar();
}
