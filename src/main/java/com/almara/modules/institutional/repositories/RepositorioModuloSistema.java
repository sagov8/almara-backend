package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.ModuloSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Módulos del Sistema.
 */
@Repository
public interface RepositorioModuloSistema extends JpaRepository<ModuloSistema, UUID> {

    Optional<ModuloSistema> findByCodigoModuloIgnoreCase(String codigoModulo);
}
