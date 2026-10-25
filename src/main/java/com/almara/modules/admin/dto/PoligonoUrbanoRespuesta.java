package com.almara.modules.admin.dto;

import com.almara.modules.admin.models.PoligonoUrbanoEspacial;
import java.time.LocalDateTime;

public record PoligonoUrbanoRespuesta(
        String idZona,
        String nombreZona,
        String descripcion,
        String geometriaGeoJson,
        Double latitudCentroide,
        Double longitudCentroide,
        int resolucionH3,
        String idCeldaH3Centroide,
        int totalCeldasH3Cubiertas,
        boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        String actualizadoPor
) {
    public static PoligonoUrbanoRespuesta fromEntidad(PoligonoUrbanoEspacial entidad) {
        return new PoligonoUrbanoRespuesta(
                entidad.getIdZona(),
                entidad.getNombreZona(),
                entidad.getDescripcion(),
                entidad.getGeometriaGeoJson(),
                entidad.getLatitudCentroide(),
                entidad.getLongitudCentroide(),
                entidad.getResolucionH3(),
                entidad.getIdCeldaH3Centroide(),
                entidad.getTotalCeldasH3Cubiertas(),
                entidad.isActivo(),
                entidad.getFechaCreacion(),
                entidad.getFechaActualizacion(),
                entidad.getActualizadoPor()
        );
    }
}
