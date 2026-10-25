package com.almara.modules.admin.repositories;

import com.almara.modules.admin.models.PoligonoUrbanoEspacial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioPoligonoUrbanoEspacial extends JpaRepository<PoligonoUrbanoEspacial, String> {

    List<PoligonoUrbanoEspacial> findAllByOrderByNombreZonaAsc();

    List<PoligonoUrbanoEspacial> findByActivoTrueOrderByNombreZonaAsc();

    Optional<PoligonoUrbanoEspacial> findByIdZonaIgnoreCase(String idZona);

    boolean existsByIdZonaIgnoreCase(String idZona);
}
