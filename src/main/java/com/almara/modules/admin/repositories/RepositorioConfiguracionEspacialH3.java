package com.almara.modules.admin.repositories;

import com.almara.modules.admin.models.ConfiguracionEspacialH3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioConfiguracionEspacialH3 extends JpaRepository<ConfiguracionEspacialH3, String> {

    default Optional<ConfiguracionEspacialH3> obtenerConfiguracionGlobal() {
        return findById(ConfiguracionEspacialH3.ID_CONFIG_GLOBAL);
    }
}
