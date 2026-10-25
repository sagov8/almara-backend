package com.almara.modules.admin.dto;

import com.almara.modules.admin.models.ConfiguracionEspacialH3;
import java.time.LocalDateTime;

public record ConfiguracionEspacialH3Respuesta(
        String id,
        int resolucionH3Default,
        int resolucionMinima,
        int resolucionMaxima,
        String descripcion,
        LocalDateTime fechaActualizacion,
        String actualizadoPor
) {
    public static ConfiguracionEspacialH3Respuesta fromEntidad(ConfiguracionEspacialH3 entidad) {
        return new ConfiguracionEspacialH3Respuesta(
                entidad.getId(),
                entidad.getResolucionH3Default(),
                entidad.getResolucionMinima(),
                entidad.getResolucionMaxima(),
                entidad.getDescripcion(),
                entidad.getFechaActualizacion(),
                entidad.getActualizadoPor()
        );
    }
}
