package com.almara.modules.admin.dto;

import com.almara.modules.admin.models.ConfiguracionMapaUmbral;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con los parámetros de configuración y umbrales del mapa emocional (HU-13).
 */
public record ConfiguracionUmbralMapaRespuesta(
        String id,
        int umbralMinimoK,
        int tiempoRecalculoSegundos,
        boolean modoEstricto,
        String mensajeProteccion,
        LocalDateTime fechaActualizacion,
        String actualizadoPor,
        int minimoTecnicoPermitidoK
) {
    public static ConfiguracionUmbralMapaRespuesta fromEntidad(ConfiguracionMapaUmbral entidad) {
        return new ConfiguracionUmbralMapaRespuesta(
                entidad.getId(),
                entidad.getUmbralMinimoK(),
                entidad.getTiempoRecalculoSegundos(),
                entidad.isModoEstricto(),
                entidad.getMensajeProteccion(),
                entidad.getFechaActualizacion(),
                entidad.getActualizadoPor(),
                ConfiguracionMapaUmbral.LIMITE_MINIMO_TECNICO_K
        );
    }
}
