package com.almara.modules.institutional.security;

import java.util.UUID;

/**
 * Representa los claims extraídos y validados de un token JWT institucional.
 */
public class ClaimsTokenInstitucional {

    private final String jti;
    private final UUID idUsuario;
    private final String correo;
    private final String razonSocial;
    private final String rol;
    private final String codigoPlan;
    private final String estadoSuscripcion;
    private final long iat;
    private final long exp;

    public ClaimsTokenInstitucional(String jti, UUID idUsuario, String correo, String razonSocial,
                                    String rol, String codigoPlan, String estadoSuscripcion,
                                    long iat, long exp) {
        this.jti = jti;
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.razonSocial = razonSocial;
        this.rol = rol;
        this.codigoPlan = codigoPlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.iat = iat;
        this.exp = exp;
    }

    public String getJti() {
        return jti;
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getRol() {
        return rol;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public long getIat() {
        return iat;
    }

    public long getExp() {
        return exp;
    }
}
