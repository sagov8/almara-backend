package com.almara.modules.institutional.dto;

import java.util.UUID;

/**
 * DTO de respuesta para la consulta de sesión activa de un cliente institucional (HU-18).
 */
public class SesionActualRespuesta {

    private UUID idInstitucion;
    private String correoInstitucional;
    private String razonSocial;
    private String rol;
    private String codigoPlan;
    private String estadoSuscripcion;
    private long expiraEnSegundosRestantes;
    private boolean activo;

    public SesionActualRespuesta() {
    }

    public SesionActualRespuesta(UUID idInstitucion, String correoInstitucional, String razonSocial,
                                 String rol, String codigoPlan, String estadoSuscripcion,
                                 long expiraEnSegundosRestantes, boolean activo) {
        this.idInstitucion = idInstitucion;
        this.correoInstitucional = correoInstitucional;
        this.razonSocial = razonSocial;
        this.rol = rol;
        this.codigoPlan = codigoPlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.expiraEnSegundosRestantes = expiraEnSegundosRestantes;
        this.activo = activo;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public long getExpiraEnSegundosRestantes() {
        return expiraEnSegundosRestantes;
    }

    public void setExpiraEnSegundosRestantes(long expiraEnSegundosRestantes) {
        this.expiraEnSegundosRestantes = expiraEnSegundosRestantes;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idInstitucion;
        private String correoInstitucional;
        private String razonSocial;
        private String rol;
        private String codigoPlan;
        private String estadoSuscripcion;
        private long expiraEnSegundosRestantes;
        private boolean activo;

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor correoInstitucional(String correoInstitucional) {
            this.correoInstitucional = correoInstitucional;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor rol(String rol) {
            this.rol = rol;
            return this;
        }

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor expiraEnSegundosRestantes(long expiraEnSegundosRestantes) {
            this.expiraEnSegundosRestantes = expiraEnSegundosRestantes;
            return this;
        }

        public Constructor activo(boolean activo) {
            this.activo = activo;
            return this;
        }

        public SesionActualRespuesta build() {
            return new SesionActualRespuesta(idInstitucion, correoInstitucional, razonSocial, rol,
                    codigoPlan, estadoSuscripcion, expiraEnSegundosRestantes, activo);
        }
    }
}
