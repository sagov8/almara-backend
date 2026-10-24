package com.almara.modules.institutional.security;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO que encapsula el resultado de la validación de acceso a un módulo del sistema (HU-20).
 */
public class ResultadoValidacionModulo {

    private final UUID idInstitucion;
    private final UUID idSuscripcion;
    private final String codigoPlan;
    private final String estadoSuscripcion;
    private final String codigoModulo;
    private final boolean accesoPermitido;
    private final Instant fechaExpiracion;

    public ResultadoValidacionModulo(UUID idInstitucion, UUID idSuscripcion, String codigoPlan,
                                     String estadoSuscripcion, String codigoModulo,
                                     boolean accesoPermitido, Instant fechaExpiracion) {
        this.idInstitucion = idInstitucion;
        this.idSuscripcion = idSuscripcion;
        this.codigoPlan = codigoPlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.codigoModulo = codigoModulo;
        this.accesoPermitido = accesoPermitido;
        this.fechaExpiracion = fechaExpiracion;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public UUID getIdSuscripcion() {
        return idSuscripcion;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public String getCodigoModulo() {
        return codigoModulo;
    }

    public boolean isAccesoPermitido() {
        return accesoPermitido;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }
}
