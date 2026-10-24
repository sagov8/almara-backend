package com.almara.modules.institutional.repositories;

import com.almara.modules.institutional.models.MatrizPermisoModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la Matriz de Permisos por Plan y Módulo.
 */
@Repository
public interface RepositorioMatrizPermisoModulo extends JpaRepository<MatrizPermisoModulo, UUID> {

    List<MatrizPermisoModulo> findByIdPlan(UUID idPlan);

    List<MatrizPermisoModulo> findByIdPlanAndModuloHabilitadoTrue(UUID idPlan);

    Optional<MatrizPermisoModulo> findByIdPlanAndIdModulo(UUID idPlan, UUID idModulo);
}
