package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.ClienteInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Clientes Institucionales.
 */
@Repository
public interface RepositorioClienteInstitucional extends JpaRepository<ClienteInstitucional, UUID> {

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByIdentificacionFiscalNit(String identificacionFiscalNit);

    Optional<ClienteInstitucional> findByCorreoIgnoreCase(String correo);

    Optional<ClienteInstitucional> findByTokenVerificacion(String tokenVerificacion);
}
