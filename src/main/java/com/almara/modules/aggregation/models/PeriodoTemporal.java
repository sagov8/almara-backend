package com.almara.modules.aggregation.models;

import java.time.Duration;

/**
 * Períodos temporales para el cálculo de métricas agregadas en zonas emocionales (HU-06).
 */
public enum PeriodoTemporal {
    ULTIMAS_2_HORAS("Últimas 2 horas", Duration.ofHours(2)),
    ULTIMAS_24_HORAS("Últimas 24 horas", Duration.ofHours(24)),
    HOY("Hoy", Duration.ofHours(24));

    private final String etiqueta;
    private final Duration duracion;

    PeriodoTemporal(String etiqueta, Duration duracion) {
        this.etiqueta = etiqueta;
        this.duracion = duracion;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public Duration getDuracion() {
        return duracion;
    }

    public static PeriodoTemporal desdeCadena(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return ULTIMAS_2_HORAS;
        }
        for (PeriodoTemporal p : values()) {
            if (p.name().equalsIgnoreCase(valor.trim()) || p.getEtiqueta().equalsIgnoreCase(valor.trim())) {
                return p;
            }
        }
        return ULTIMAS_2_HORAS;
    }
}
