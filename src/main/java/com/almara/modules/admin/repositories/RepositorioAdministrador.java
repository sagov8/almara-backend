package com.almara.modules.admin.repositories;

import com.almara.modules.admin.models.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad Administrador.
 */
@Repository
public interface RepositorioAdministrador extends JpaRepository<Administrador, UUID> {

    Optional<Administrador> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}
