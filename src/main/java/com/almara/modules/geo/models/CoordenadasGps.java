package com.almara.modules.geo.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Objeto de valor efímero para representar coordenadas geográficas GPS decimales.
 * Únicamente existe en memoria durante la conversión a celda H3
 * y no se persiste ni se expone externamente.
 */
public class CoordenadasGps {

    @NotNull(message = "La latitud es requerida")
    @DecimalMin(value = "-90.0", message = "La latitud mínima válida es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima válida es 90.0")
    private Double latitud;

    @NotNull(message = "La longitud es requerida")
    @DecimalMin(value = "-180.0", message = "La longitud mínima válida es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima válida es 180.0")
    private Double longitud;

    public CoordenadasGps() {
    }

    public CoordenadasGps(Double latitud, Double longitud) {
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    @Override
    public String toString() {
        // Por privacidad nunca se imprimime las coordenadas exactas en toString/logs
        return "CoordenadasGps[PROTEGIDAS_POR_PRIVACIDAD]";
    }
}
