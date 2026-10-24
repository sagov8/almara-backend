package com.almara.modules.admin.security;

import java.util.UUID;

/**
 * Claims estructurados del token de sesión JWT emitido para un Administrador (HU-11).
 */
public class ClaimsTokenAdmin {

    private final UUID idAdmin;
    private final String correo;
    private final String nombreCompleto;
    private final String rol;
    private final String jti;
    private final long iat;
    private final long exp;

    public ClaimsTokenAdmin(UUID idAdmin, String correo, String nombreCompleto, String rol,
                            String jti, long iat, long exp) {
        this.idAdmin = idAdmin;
        this.correo = correo;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.jti = jti;
        this.iat = iat;
        this.exp = exp;
    }

    public UUID getIdAdmin() {
        return idAdmin;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public String getJti() {
        return jti;
    }

    public long getIat() {
        return iat;
    }

    public long getExp() {
        return exp;
    }
}
