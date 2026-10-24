package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.CarritoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para el Carrito de Compras Institucional (HU-22).
 */
@Repository
public interface RepositorioCarritoCompra extends JpaRepository<CarritoCompra, UUID> {

    Optional<CarritoCompra> findByIdInstitucion(UUID idInstitucion);
}
