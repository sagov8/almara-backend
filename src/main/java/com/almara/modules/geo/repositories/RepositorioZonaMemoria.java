package com.almara.modules.geo.repositories;

import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.services.AdaptadorH3;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación en memoria del repositorio de zonas urbanas.
 * Precarga el catálogo predefinido de sectores urbanos de referencia
 * con celdas H3 válidas calculadas a la resolución 9 prefijada (HU-04).
 */
@Repository
public class RepositorioZonaMemoria implements RepositorioZona {

    private final AdaptadorH3 adaptadorH3;
    private final Map<String, Zona> zonasPorCeldaH3 = new ConcurrentHashMap<>();
    private final Map<String, Zona> zonasPorManualId = new ConcurrentHashMap<>();

    public RepositorioZonaMemoria(AdaptadorH3 adaptadorH3) {
        this.adaptadorH3 = adaptadorH3;
    }

    @PostConstruct
    public void inicializarCatalogoPredeterminado() {
        // Catálogo de zonas representativas (Bogotá como referencia urbana del proyecto)
        precargarZona("ZONA-CHAPINERO", "Chapinero Central", 4.6486, -74.0645, 9);
        precargarZona("ZONA-USAQUEN", "Usaquén", 4.6974, -74.0298, 9);
        precargarZona("ZONA-TEUSAQUILLO", "Teusaquillo / Parkway", 4.6315, -74.0817, 9);
        precargarZona("ZONA-CENTRO", "La Candelaria / Centro Histórico", 4.5981, -74.0758, 9);
        precargarZona("ZONA-SUBA", "Suba Centro", 4.7431, -74.0886, 9);
        precargarZona("ZONA-SALITRE", "Ciudad Salitre", 4.6534, -74.1086, 9);
    }

    private void precargarZona(String manualId, String nombre, double lat, double lon, int resolucion) {
        String idCeldaH3 = adaptadorH3.coordenadasACelda(lat, lon, resolucion);
        CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCeldaH3);

        Zona zona = Zona.builder()
                .idCeldaH3(idCeldaH3)
                .resolucionH3(resolucion)
                .latitudCentroide(centroide.getLatitud())
                .longitudCentroide(centroide.getLongitud())
                .zonaManualId(manualId)
                .nombreZonaManual(nombre)
                .umbralMinimo(5)
                .build();

        guardar(zona);
    }

    @Override
    public Optional<Zona> buscarPorIdCeldaH3(String idCeldaH3) {
        if (idCeldaH3 == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(zonasPorCeldaH3.get(idCeldaH3));
    }

    @Override
    public Optional<Zona> buscarPorZonaManualId(String zonaManualId) {
        if (zonaManualId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(zonasPorManualId.get(zonaManualId.trim().toUpperCase()));
    }

    @Override
    public List<Zona> obtenerZonasManuales() {
        return new ArrayList<>(zonasPorManualId.values());
    }

    @Override
    public Zona guardar(Zona zona) {
        zonasPorCeldaH3.put(zona.getIdCeldaH3(), zona);
        if (zona.getZonaManualId() != null) {
            zonasPorManualId.put(zona.getZonaManualId().toUpperCase(), zona);
        }
        return zona;
    }
}
