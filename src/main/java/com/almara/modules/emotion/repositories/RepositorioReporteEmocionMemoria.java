package com.almara.modules.emotion.repositories;

import com.almara.modules.emotion.models.ReporteEmocion;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación concurrente en memoria del repositorio de reportes de emoción.
 * Asegura alta concurrencia, respuesta < 1000 ms y aislamiento para pruebas.
 */
@Repository
public class RepositorioReporteEmocionMemoria implements RepositorioReporteEmocion {

    private final Map<UUID, ReporteEmocion> reportesPorId = new ConcurrentHashMap<>();

    @Override
    public ReporteEmocion guardar(ReporteEmocion reporte) {
        if (reporte.getIdEvento() == null) {
            reporte.setIdEvento(UUID.randomUUID());
        }
        reportesPorId.put(reporte.getIdEvento(), reporte);
        return reporte;
    }

    @Override
    public Optional<ReporteEmocion> buscarPorId(UUID idEvento) {
        if (idEvento == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(reportesPorId.get(idEvento));
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
        return reportesPorId.size();
    }

    @Override
    public List<ReporteEmocion> obtenerTodos() {
        return new ArrayList<>(reportesPorId.values());
    }

    @Override
    public void limpiar() {
        reportesPorId.clear();
    }
}
