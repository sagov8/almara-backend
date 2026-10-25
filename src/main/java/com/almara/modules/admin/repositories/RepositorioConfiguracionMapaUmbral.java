package com.almara.modules.admin.repositories;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la persistencia de umbrales del mapa y políticas de k-anonimato (HU-13).
 */
@Repository
public interface RepositorioConfiguracionMapaUmbral extends JpaRepository<ConfiguracionMapaUmbral, String> {

    default Optional<ConfiguracionMapaUmbral> obtenerConfiguracionGlobal() {
        return findById(ConfiguracionMapaUmbral.ID_CONFIGURACION_GLOBAL);
    }
}
