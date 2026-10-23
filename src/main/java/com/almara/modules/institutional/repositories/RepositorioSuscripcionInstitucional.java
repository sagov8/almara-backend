package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.SuscripcionInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Suscripciones Institucionales.
 */
@Repository
public interface RepositorioSuscripcionInstitucional extends JpaRepository<SuscripcionInstitucional, UUID> {

    Optional<SuscripcionInstitucional> findFirstByIdInstitucionOrderByFechaExpiracionDesc(UUID idInstitucion);

    List<SuscripcionInstitucional> findByIdInstitucionOrderByFechaInicioDesc(UUID idInstitucion);
}
