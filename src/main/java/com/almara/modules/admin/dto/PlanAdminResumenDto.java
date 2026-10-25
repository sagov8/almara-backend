package com.almara.modules.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resumen de plan de suscripción disponible para asignación administrativa (HU-15).
 */
public record PlanAdminResumenDto(
        UUID idProducto,
        String codigo,
        String nombre,
        String descripcion,
        BigDecimal precioBase,
        String tipoPlan,
        int duracionDias,
        boolean esGratuito,
        boolean activo
) {
}
