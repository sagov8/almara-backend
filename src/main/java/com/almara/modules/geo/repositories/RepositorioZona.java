package com.almara.modules.geo.repositories;

import com.almara.modules.geo.models.Zona;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia para zonas urbanas y celdas H3 (Patrón Repository).
 */
public interface RepositorioZona {

    /**
     * Busca una zona por su identificador de celda H3.
     */
    Optional<Zona> buscarPorIdCeldaH3(String idCeldaH3);

    /**
     * Busca una zona por su identificador del catálogo manual (ej: ZONA-CHAPINERO).
     */
    Optional<Zona> buscarPorZonaManualId(String zonaManualId);

    /**
     * Retorna todas las zonas configuradas para selección manual en la interfaz.
     */
    List<Zona> obtenerZonasManuales();

    /**
     * Guarda o actualiza una zona.
     */
    Zona guardar(Zona zona);
}
