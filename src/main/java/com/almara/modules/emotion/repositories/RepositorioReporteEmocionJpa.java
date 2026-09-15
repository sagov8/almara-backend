package com.almara.modules.emotion.repositories;

import com.almara.modules.emotion.models.EntidadReporteEmocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio Spring Data JPA para la tabla reporte_emocion.
 */
@Repository
public interface RepositorioReporteEmocionJpa extends JpaRepository<EntidadReporteEmocion, UUID> {

    List<EntidadReporteEmocion> findByIdCeldaH3IgnoreCase(String idCeldaH3);
}
