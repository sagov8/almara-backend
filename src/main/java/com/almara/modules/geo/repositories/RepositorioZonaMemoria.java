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
import com.almara.modules.geo.models.EntidadZona;
import org.springframework.beans.factory.ObjectProvider;

@Repository
public class RepositorioZonaMemoria implements RepositorioZona {

    private final AdaptadorH3 adaptadorH3;
    private final RepositorioZonaJpa repositorioZonaJpa;
    private final Map<String, Zona> zonasPorCeldaH3 = new ConcurrentHashMap<>();
    private final Map<String, Zona> zonasPorManualId = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public RepositorioZonaMemoria(AdaptadorH3 adaptadorH3, ObjectProvider<RepositorioZonaJpa> jpaProvider) {
        this.adaptadorH3 = adaptadorH3;
        this.repositorioZonaJpa = jpaProvider != null ? jpaProvider.getIfAvailable() : null;
    }

    public RepositorioZonaMemoria(AdaptadorH3 adaptadorH3) {
        this.adaptadorH3 = adaptadorH3;
        this.repositorioZonaJpa = null;
    }

    @PostConstruct
    public void inicializarCatalogoPredeterminado() {
        if (repositorioZonaJpa != null) {
            List<EntidadZona> zonasDb = repositorioZonaJpa.findAll();
            if (!zonasDb.isEmpty()) {
                for (EntidadZona ez : zonasDb) {
                    Zona z = Zona.builder()
                            .idCeldaH3(ez.getIdCeldaH3())
                            .resolucionH3(ez.getResolucionH3())
                            .latitudCentroide(ez.getLatitudCentroide())
                            .longitudCentroide(ez.getLongitudCentroide())
                            .zonaManualId(ez.getZonaManualId())
                            .nombreZonaManual(ez.getNombreZonaManual())
                            .descripcion(ez.getDescripcion())
                            .umbralMinimo(ez.getUmbralMinimo())
                            .build();
                    guardar(z);
                }
                return;
            }
        }

        // Catálogo de 15 zonas emblemáticas de Popayán (HU-04)
        precargarZona("ZONA-PARQUE-CALDAS", "Parque Caldas", "El eje fundacional, rodeado por la Catedral Basílica, Torre del Reloj y sedes de gobierno.", 2.4419, -76.6063, 9);
        precargarZona("ZONA-HUMILLADERO", "Puente del Humilladero", "Estructura de once arcos de ladrillo y calicanto sobre la depresión del río Molino.", 2.4456, -76.6058, 9);
        precargarZona("ZONA-MORRO-TULCAN", "El Morro de Tulcán", "Sitio arqueológico prehispánico y mirador natural emblemático de Popayán.", 2.4452, -76.5997, 9);
        precargarZona("ZONA-ERMITA", "Ermita de Jesús Nazareno", "La iglesia más antigua en pie de la ciudad (siglo XVI) sobre colina con vista al centro.", 2.4406, -76.6014, 9);
        precargarZona("ZONA-PANTEON-PROCERES", "Panteón de los Próceres", "Antiguo templo de Santo Domingo reconvertido en mausoleo de próceres y expresidentes.", 2.4428, -76.6074, 9);
        precargarZona("ZONA-TERRA-PLAZA", "Centro Comercial Terra Plaza", "Principal nodo de comercio moderno, servicios y entretenimiento del sector norte.", 2.4860, -76.5648, 9);
        precargarZona("ZONA-CAMPUS-TULCAN", "Campus Tulcán (Unicauca)", "Centro neurálgico universitario y académico público entre el centro y el norte.", 2.4475, -76.6010, 9);
        precargarZona("ZONA-BELLA-VISTA", "Sector de Bella Vista", "Área residencial y comercial contemporánea, referente de salud e institutos técnicos.", 2.4580, -76.5975, 9);
        precargarZona("ZONA-VILLA-OLIMPICA", "Villa Olímpica", "Complejo deportivo central para atletismo, natación y fútbol sobre la Carrera 6.", 2.4645, -76.5910, 9);
        precargarZona("ZONA-RINCON-PAYANES", "Rincón Payanés (Pueblito Patojo)", "Réplica a escala de monumentos coloniales rodeada de zonas verdes y gastronomía.", 2.4450, -76.6042, 9);
        precargarZona("ZONA-YANACONAS", "Sector Calicanto / Yanaconas", "Enclave periurbano tradicional con arquitectura colonial rural e iglesia de San José.", 2.4460, -76.5865, 9);
        precargarZona("ZONA-PARQUE-SALUD", "Parque de la Salud (Los Hoyos)", "Espacio recreativo y sendero ecológico para actividad física matutina al aire libre.", 2.4485, -76.6080, 9);
        precargarZona("ZONA-LA-ESMERALDA", "Galería La Esmeralda", "Centro de abastos y distribución alimentaria más activo del sur de Popayán.", 2.4402, -76.6190, 9);
        precargarZona("ZONA-EL-EMPEDRADO", "Sector El Empedrado", "Barrio tradicional del sur del centro con calles empinadas de piedra y talleres artesanales.", 2.4385, -76.6060, 9);
        precargarZona("ZONA-CAMPANARIO", "Centro Comercial Campanario", "Punto bisagra entre el centro y norte, nodo de encuentro social y financiero de mayor flujo.", 2.4578, -76.5930, 9);
    }

    private void precargarZona(String manualId, String nombre, String descripcion, double lat, double lon, int resolucion) {
        String idCeldaH3 = adaptadorH3.coordenadasACelda(lat, lon, resolucion);
        CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCeldaH3);

        Zona zona = Zona.builder()
                .idCeldaH3(idCeldaH3)
                .resolucionH3(resolucion)
                .latitudCentroide(centroide.getLatitud())
                .longitudCentroide(centroide.getLongitud())
                .zonaManualId(manualId)
                .nombreZonaManual(nombre)
                .descripcion(descripcion)
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
