package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Pagos y Transacciones de Suscripciones SaaS (HU-23).
 */
@Repository
public interface RepositorioPago extends JpaRepository<Pago, UUID> {

    List<Pago> findByIdOrdenOrderByFechaTransaccionDesc(UUID idOrden);

    Optional<Pago> findFirstByIdOrdenAndEstadoPago(UUID idOrden, String estadoPago);
}
