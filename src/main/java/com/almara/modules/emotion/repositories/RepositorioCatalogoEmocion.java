package com.almara.modules.emotion.repositories;

import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la tabla catalogo_emocion.
 * Permite consultar el catálogo persistido y filtrado por estado activo.
 */
@Repository
public interface RepositorioCatalogoEmocion extends JpaRepository<EntidadCatalogoEmocion, String> {

    List<EntidadCatalogoEmocion> findByActivoTrue();
}
