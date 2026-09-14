package com.almara.modules.geo.services;

import com.almara.common.ExcepcionZonaNoEncontrada;
import com.almara.modules.geo.models.ResolucionZonaContexto;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Estrategia de resolución de celda H3 a través del catálogo predefinido de zonas (HU-04).
 * Se utiliza cuando el usuario no otorga permisos de geolocalización y selecciona manualmente su sector.
 */
@Component
@Order(2)
public class ManualCatalogResolutionStrategy implements EstrategiaResolucionZona {

    private static final Logger log = LoggerFactory.getLogger(ManualCatalogResolutionStrategy.class);

    private final RepositorioZona repositorioZona;

    public ManualCatalogResolutionStrategy(RepositorioZona repositorioZona) {
        this.repositorioZona = repositorioZona;
    }

    @Override
    public boolean aplica(ResolucionZonaContexto contexto) {
        return contexto != null && contexto.tieneZonaManual();
    }

    @Override
    public ResultadoResolucionZona resolver(ResolucionZonaContexto contexto) {
        String zonaManualId = contexto.getZonaManualId();

        Zona zona = repositorioZona.buscarPorZonaManualId(zonaManualId)
                .orElseThrow(() -> new ExcepcionZonaNoEncontrada(
                        "La zona manual '" + zonaManualId + "' no existe en el catálogo de zonas predefinidas"));

        log.info("Celda H3 resuelta por catálogo manual: [Zona: {}, Celda: {}]",
                zona.getNombreZonaManual(), zona.getIdCeldaH3());

        return ResultadoResolucionZona.builder()
                .idCeldaH3(zona.getIdCeldaH3())
                .resolucionH3(zona.getResolucionH3())
                .nombreZona(zona.getNombreZonaManual())
                .zonaManualId(zona.getZonaManualId())
                .esManual(true)
                .latitudCentroide(zona.getLatitudCentroide())
                .longitudCentroide(zona.getLongitudCentroide())
                .build();
    }
}
