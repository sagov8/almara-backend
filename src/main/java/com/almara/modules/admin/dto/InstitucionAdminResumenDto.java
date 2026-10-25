package com.almara.modules.admin.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de resumen de Cliente Institucional para vistas administrativas (HU-15).
 */
public record InstitucionAdminResumenDto(
        UUID idInstitucion,
        String razonSocial,
        String identificacionFiscalNit,
        String correo,
        String nombreContacto,
        String telefonoContacto,
        String sectorActividad,
        String estadoCuenta,
        boolean correoVerificado,
        boolean activo,
        Instant fechaRegistro,
        UUID idPlan,
        String codigoPlan,
        String nombrePlan,
        String estadoSuscripcion,
        Instant fechaExpiracionSuscripcion
) {
}
