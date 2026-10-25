package com.almara.modules.admin.repositories;

import com.almara.modules.admin.models.AuditoriaAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para la bitácora de auditoría administrativa.
 */
@Repository
public interface RepositorioAuditoriaAdmin extends JpaRepository<AuditoriaAdmin, UUID> {

    List<AuditoriaAdmin> findTop50ByOrderByFechaEventoDesc();

    List<AuditoriaAdmin> findByIdAdminOrderByFechaEventoDesc(UUID idAdmin);

    List<AuditoriaAdmin> findByAccionOrderByFechaEventoDesc(String accion);
}
