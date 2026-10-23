package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Planes de Suscripción SaaS.
 */
@Repository
public interface RepositorioPlanSuscripcion extends JpaRepository<PlanSuscripcion, UUID> {

    List<PlanSuscripcion> findByActivoTrueOrderByPrecioBaseAsc();

    Optional<PlanSuscripcion> findByCodigoIgnoreCase(String codigo);
}
