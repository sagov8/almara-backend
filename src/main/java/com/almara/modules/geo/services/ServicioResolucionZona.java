package com.almara.modules.geo.services;

import com.almara.common.ExcepcionUbicacionRequerida;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.ElementoCatalogoZona;
import com.almara.modules.geo.models.ResolucionZonaContexto;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import com.almara.modules.geo.repositories.RepositorioZona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio central del módulo geoespacial (HU-04).
 * Orquesta la resolución de zonas a celdas H3 mediante el patrón Strategy
 * y provee el catálogo de zonas predefinidas para la selección manual ciudadana.
 */
@Service
public class ServicioResolucionZona {

    private final List<EstrategiaResolucionZona> estrategias;
    private final RepositorioZona repositorioZona;
    private final int resolucionPredeterminada;

    public ServicioResolucionZona(
            List<EstrategiaResolucionZona> estrategias,
            RepositorioZona repositorioZona,
            @Value("${almara.h3.default-resolution:9}") int resolucionPredeterminada) {
        this.estrategias = estrategias;
        this.repositorioZona = repositorioZona;
        this.resolucionPredeterminada = resolucionPredeterminada;
    }

    /**
     * Resuelve la celda H3 a partir de coordenadas GPS o un identificador de catálogo manual.
     * Si no se provee ninguna de las dos alternativas, rechaza la operación (Criterio 4 de HU-04).
     */
    public ResultadoResolucionZona resolverZona(CoordenadasGps coordenadasGps, String zonaManualId) {
        ResolucionZonaContexto contexto = ResolucionZonaContexto.builder()
                .coordenadasGps(coordenadasGps)
                .zonaManualId(zonaManualId)
                .resolucionDeseada(resolucionPredeterminada)
                .build();

        for (EstrategiaResolucionZona estrategia : estrategias) {
            if (estrategia.aplica(contexto)) {
                return estrategia.resolver(contexto);
            }
        }

        throw new ExcepcionUbicacionRequerida(
                "Se requiere autorización de ubicación GPS o selección manual de una zona predefinida para registrar la emoción");
    }

    /**
     * Retorna la lista de sectores disponibles para selección manual cuando se niega el permiso GPS.
     */
    public List<ElementoCatalogoZona> obtenerCatalogoZonasManuales() {
        return repositorioZona.obtenerZonasManuales().stream()
                .map(z -> ElementoCatalogoZona.builder()
                        .zonaManualId(z.getZonaManualId())
                        .nombre(z.getNombreZonaManual())
                        .descripcion(z.getDescripcion())
                        .idCeldaH3(z.getIdCeldaH3())
                        .resolucionH3(z.getResolucionH3())
                        .latitudCentroide(z.getLatitudCentroide())
                        .longitudCentroide(z.getLongitudCentroide())
                        .build())
                .toList();
    }

    public int getResolucionPredeterminada() {
        return resolucionPredeterminada;
    }
}
