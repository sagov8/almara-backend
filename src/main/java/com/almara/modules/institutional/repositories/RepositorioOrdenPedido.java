package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.OrdenPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Órdenes de Pedido de Suscripciones SaaS (HU-21, HU-22, HU-23).
 */
@Repository
public interface RepositorioOrdenPedido extends JpaRepository<OrdenPedido, UUID> {

    List<OrdenPedido> findByIdInstitucionOrderByFechaEmisionDesc(UUID idInstitucion);

    Optional<OrdenPedido> findByNumeroOrden(String numeroOrden);

    Optional<OrdenPedido> findFirstByIdInstitucionAndIdPlanSeleccionadoAndEstadoOrden(
            UUID idInstitucion, UUID idPlanSeleccionado, String estadoOrden);
}
