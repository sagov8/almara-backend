package com.almara.modules.aggregation.repositories;

import com.almara.modules.aggregation.models.EntidadEstadoEmocionalZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la tabla estado_emocional_zona.
 */
@Repository
public interface RepositorioEstadoEmocionalZonaJpa extends JpaRepository<EntidadEstadoEmocionalZona, String> {

    List<EntidadEstadoEmocionalZona> findByCumpleUmbralTrue();
}
