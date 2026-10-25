package com.almara.modules.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearPoligonoUrbanoSolicitud(
        @NotBlank(message = "El identificador de zona es obligatorio")
        @Pattern(regexp = "^[A-Z0-9_-]{3,50}$", message = "El identificador de zona debe ser en mayúsculas, números o guiones (ej. ZONA-COMUNA-1)")
        String idZona,

        @NotBlank(message = "El nombre de la zona es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombreZona,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion,

        @NotBlank(message = "La geometría GeoJSON del polígono es obligatoria")
        String geometriaGeoJson,

        @Min(value = 7, message = "La resolución H3 mínima permitida es 7")
        @Max(value = 10, message = "La resolución H3 máxima permitida es 10 (RF-09)")
        Integer resolucionH3,

        @NotBlank(message = "Debe proporcionar una justificación técnica para el log de auditoría (RNF-13)")
        @Size(max = 250, message = "La justificación no puede exceder 250 caracteres")
        String justificacionAuditoria
) {
}
