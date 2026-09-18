package com.almara.modules.emotion.repositories;

import com.almara.modules.emotion.models.EntidadReporteEmocion;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.EntidadZona;
import com.almara.modules.geo.repositories.RepositorioZonaJpa;
import com.almara.modules.geo.services.AdaptadorH3;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación híbrida del repositorio de reportes de emoción (Patrón Adapter/Repository).
 * Persiste de forma duradera en PostgreSQL (JPA) para habilitar analítica histórica e institucional (HU-16),
 * manteniendo una caché concurrente en memoria para respuestas de baja latencia (< 1000 ms, HU-01).
 */
@Repository
public class RepositorioReporteEmocionMemoria implements RepositorioReporteEmocion {

    private static final Logger log = LoggerFactory.getLogger(RepositorioReporteEmocionMemoria.class);

    private final RepositorioReporteEmocionJpa repositorioReporteEmocionJpa;
    private final RepositorioZonaJpa repositorioZonaJpa;
    private final AdaptadorH3 adaptadorH3;
    private final Map<UUID, ReporteEmocion> reportesPorId = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    public RepositorioReporteEmocionMemoria(
            ObjectProvider<RepositorioReporteEmocionJpa> jpaProvider,
            ObjectProvider<RepositorioZonaJpa> zonaJpaProvider,
            ObjectProvider<AdaptadorH3> adaptadorH3Provider) {
        this.repositorioReporteEmocionJpa = jpaProvider != null ? jpaProvider.getIfAvailable() : null;
        this.repositorioZonaJpa = zonaJpaProvider != null ? zonaJpaProvider.getIfAvailable() : null;
        this.adaptadorH3 = adaptadorH3Provider != null ? adaptadorH3Provider.getIfAvailable() : null;
    }

    public RepositorioReporteEmocionMemoria() {
        this.repositorioReporteEmocionJpa = null;
        this.repositorioZonaJpa = null;
        this.adaptadorH3 = null;
    }

    @PostConstruct
    public void inicializarDesdeBaseDeDatos() {
        if (repositorioReporteEmocionJpa != null) {
            try {
                List<EntidadReporteEmocion> entidades = repositorioReporteEmocionJpa.findAll();
                for (EntidadReporteEmocion entidad : entidades) {
                    ReporteEmocion reporte = mapearADominio(entidad);
                    reportesPorId.put(reporte.getIdEvento(), reporte);
                }
                log.info("Cargados {} reportes de emoción persistidos desde PostgreSQL a memoria.", entidades.size());
            } catch (Exception ex) {
                log.warn("No fue posible precargar reportes desde PostgreSQL: {}", ex.getMessage());
            }
        }
    }

    @Override
    public ReporteEmocion guardar(ReporteEmocion reporte) {
        if (reporte.getIdEvento() == null) {
            reporte.setIdEvento(UUID.randomUUID());
        }
        if (reporte.getFechaHora() == null) {
            reporte.setFechaHora(Instant.now());
        }

        // 1. Garantizar integridad referencial con la tabla zona
        asegurarExistenciaZona(reporte.getIdCeldaH3());

        // 2. Persistir en PostgreSQL
        if (repositorioReporteEmocionJpa != null) {
            try {
                EntidadReporteEmocion entidad = EntidadReporteEmocion.builder()
                        .idEvento(reporte.getIdEvento())
                        .idCeldaH3(reporte.getIdCeldaH3())
                        .idEmocion(reporte.getEmocion() != null ? reporte.getEmocion().name() : null)
                        .intensidad(reporte.getIntensidad() != null ? reporte.getIntensidad().floatValue() : null)
                        .comentario(reporte.getComentario())
                        .tokenSesionTemporal(reporte.getTokenSesionTemporal())
                        .fechaHora(reporte.getFechaHora())
                        .build();
                repositorioReporteEmocionJpa.save(entidad);
            } catch (Exception ex) {
                log.warn("Error al persistir reporte de emoción en PostgreSQL, manteniendo copia en memoria: {}", ex.getMessage());
            }
        }

        // 3. Mantener en memoria para acceso ultra rápido
        reportesPorId.put(reporte.getIdEvento(), reporte);
        return reporte;
    }

    @Override
    public Optional<ReporteEmocion> buscarPorId(UUID idEvento) {
        if (idEvento == null) {
            return Optional.empty();
        }

        ReporteEmocion enMemoria = reportesPorId.get(idEvento);
        if (enMemoria != null) {
            return Optional.of(enMemoria);
        }

        if (repositorioReporteEmocionJpa != null) {
            try {
                return repositorioReporteEmocionJpa.findById(idEvento)
                        .map(this::mapearADominio)
                        .map(r -> {
                            reportesPorId.put(r.getIdEvento(), r);
                            return r;
                        });
            } catch (Exception ex) {
                log.warn("Error al consultar reporte en PostgreSQL: {}", ex.getMessage());
            }
        }

        return Optional.empty();
    }

    @Override
    public List<ReporteEmocion> buscarPorCeldaH3(String idCeldaH3) {
        if (idCeldaH3 == null) {
            return List.of();
        }
        return reportesPorId.values().stream()
                .filter(r -> idCeldaH3.equalsIgnoreCase(r.getIdCeldaH3()))
                .toList();
    }

    @Override
    public long contarTotal() {
        if (reportesPorId.isEmpty() && repositorioReporteEmocionJpa != null) {
            try {
                return repositorioReporteEmocionJpa.count();
            } catch (Exception ignored) {
            }
        }
        return reportesPorId.size();
    }

    @Override
    public List<ReporteEmocion> obtenerTodos() {
        return new ArrayList<>(reportesPorId.values());
    }

    @Override
    public void limpiar() {
        reportesPorId.clear();
        if (repositorioReporteEmocionJpa != null) {
            try {
                repositorioReporteEmocionJpa.deleteAll();
            } catch (Exception ignored) {
            }
        }
    }

    private void asegurarExistenciaZona(String idCeldaH3) {
        if (repositorioZonaJpa == null || idCeldaH3 == null) {
            return;
        }

        try {
            if (!repositorioZonaJpa.existsById(idCeldaH3)) {
                double lat = 2.4434;
                double lon = -76.6056;
                int res = 9;

                if (adaptadorH3 != null && adaptadorH3.esCeldaValida(idCeldaH3)) {
                    CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCeldaH3);
                    if (centroide != null) {
                        lat = centroide.getLatitud();
                        lon = centroide.getLongitud();
                    }
                    res = adaptadorH3.obtenerResolucion(idCeldaH3);
                }

                EntidadZona nuevaZona = EntidadZona.builder()
                        .idCeldaH3(idCeldaH3)
                        .resolucionH3(res)
                        .latitudCentroide(lat)
                        .longitudCentroide(lon)
                        .nombreZonaManual("Celda H3 " + idCeldaH3)
                        .umbralMinimo(5)
                        .build();

                repositorioZonaJpa.save(nuevaZona);
                log.info("Zona dinámica auto-registrada para preservar FK: [Celda: {}]", idCeldaH3);
            }
        } catch (Exception ex) {
            log.warn("No fue posible verificar o auto-registrar la zona [{}]: {}", idCeldaH3, ex.getMessage());
        }
    }

    private ReporteEmocion mapearADominio(EntidadReporteEmocion entidad) {
        TipoEmocion tipo = null;
        if (entidad.getIdEmocion() != null) {
            try {
                tipo = TipoEmocion.valueOf(entidad.getIdEmocion());
            } catch (Exception ignored) {
            }
        }

        return ReporteEmocion.builder()
                .idEvento(entidad.getIdEvento())
                .idCeldaH3(entidad.getIdCeldaH3())
                .emocion(tipo)
                .intensidad(entidad.getIntensidad())
                .comentario(entidad.getComentario())
                .tokenSesionTemporal(entidad.getTokenSesionTemporal())
                .fechaHora(entidad.getFechaHora())
                .build();
    }
}
