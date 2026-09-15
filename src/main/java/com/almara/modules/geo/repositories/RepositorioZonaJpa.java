package com.almara.modules.geo.repositories;

import com.almara.modules.geo.models.EntidadZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la tabla zona.
 * Permite consultar sectores por identificador manual y por celda H3.
 */
@Repository
public interface RepositorioZonaJpa extends JpaRepository<EntidadZona, String> {

    Optional<EntidadZona> findByZonaManualIdIgnoreCase(String zonaManualId);

    List<EntidadZona> findByZonaManualIdIsNotNull();
}
