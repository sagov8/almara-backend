package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.AuditoriaInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para Auditoría Institucional.
 */
@Repository
public interface RepositorioAuditoriaInstitucional extends JpaRepository<AuditoriaInstitucional, UUID> {

    List<AuditoriaInstitucional> findByIdEntidadAfectadaOrderByFechaEventoDesc(UUID idEntidadAfectada);
}
