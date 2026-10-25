package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de actualización de los umbrales de privacidad y agregación del mapa (HU-13).
 */
public record ActualizarUmbralMapaSolicitud(
        @NotNull(message = "El umbral mínimo de k-anonimato es obligatorio")
        @Min(value = 5, message = "El umbral mínimo k no puede ser inferior al límite técnico de privacidad (k >= 5)")
        Integer umbralMinimoK,

        @NotNull(message = "El tiempo de recálculo en segundos es obligatorio")
        @Min(value = 10, message = "El tiempo de recálculo debe ser de al menos 10 segundos")
        Integer tiempoRecalculoSegundos,

        Boolean modoEstricto,

        @NotBlank(message = "El mensaje de protección ciudadana no puede estar vacío")
        @Size(max = 500, message = "El mensaje de protección no puede superar 500 caracteres")
        String mensajeProteccion,

        @NotBlank(message = "Debe proporcionar una justificación técnica para el log de auditoría")
        @Size(max = 250, message = "La justificación no puede exceder 250 caracteres")
        String justificacionAuditoria
) {
}
