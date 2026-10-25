package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarResolucionH3Solicitud(
        @NotNull(message = "La resolución espacial H3 es obligatoria")
        @Min(value = 7, message = "La resolución H3 mínima permitida es 7 para evitar celdas excesivamente grandes")
        @Max(value = 10, message = "La resolución H3 máxima permitida es 10 para proteger el anonimato (RF-09)")
        Integer resolucionH3Default,

        @NotBlank(message = "Debe proporcionar una justificación técnica para el log de auditoría (RNF-13)")
        @Size(max = 250, message = "La justificación no puede exceder 250 caracteres")
        String justificacionAuditoria
) {
}
