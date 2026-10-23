package com.almara.modules.institutional.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO para la respuesta de inicio de sesión institucional con JWT y claims de suscripción (HU-17).
 */
public class LoginInstitucionalRespuesta {

    private String tokenAcceso;
    private String tipoToken = "Bearer";
    private long expiraEnSegundos;
    private UUID idInstitucion;
    private String razonSocial;
    private String correoInstitucional;
    private String rol;
    private String codigoPlan;
    private String nombrePlan;
    private String estadoSuscripcion;
    private List<String> modulosHabilitados;
    private boolean requiereRenovacion;
    private String rutaRedireccion;

    public LoginInstitucionalRespuesta() {
    }

    public LoginInstitucionalRespuesta(String tokenAcceso, String tipoToken, long expiraEnSegundos,
                                       UUID idInstitucion, String razonSocial, String correoInstitucional,
                                       String rol, String codigoPlan, String nombrePlan,
                                       String estadoSuscripcion, List<String> modulosHabilitados,
                                       boolean requiereRenovacion, String rutaRedireccion) {
        this.tokenAcceso = tokenAcceso;
        this.tipoToken = tipoToken != null ? tipoToken : "Bearer";
        this.expiraEnSegundos = expiraEnSegundos;
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.correoInstitucional = correoInstitucional;
        this.rol = rol;
        this.codigoPlan = codigoPlan;
        this.nombrePlan = nombrePlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.modulosHabilitados = modulosHabilitados;
        this.requiereRenovacion = requiereRenovacion;
        this.rutaRedireccion = rutaRedireccion;
    }

    public String getTokenAcceso() {
        return tokenAcceso;
    }

    public void setTokenAcceso(String tokenAcceso) {
        this.tokenAcceso = tokenAcceso;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public void setExpiraEnSegundos(long expiraEnSegundos) {
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCorreoInstitucional() {
        return correoInstitucional;
    }

    public void setCorreoInstitucional(String correoInstitucional) {
        this.correoInstitucional = correoInstitucional;
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

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public List<String> getModulosHabilitados() {
        return modulosHabilitados;
    }

    public void setModulosHabilitados(List<String> modulosHabilitados) {
        this.modulosHabilitados = modulosHabilitados;
    }

    public boolean isRequiereRenovacion() {
        return requiereRenovacion;
    }

    public void setRequiereRenovacion(boolean requiereRenovacion) {
        this.requiereRenovacion = requiereRenovacion;
    }

    public String getRutaRedireccion() {
        return rutaRedireccion;
    }

    public void setRutaRedireccion(String rutaRedireccion) {
        this.rutaRedireccion = rutaRedireccion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String tokenAcceso;
        private String tipoToken = "Bearer";
        private long expiraEnSegundos;
        private UUID idInstitucion;
        private String razonSocial;
        private String correoInstitucional;
        private String rol;
        private String codigoPlan;
        private String nombrePlan;
        private String estadoSuscripcion;
        private List<String> modulosHabilitados;
        private boolean requiereRenovacion;
        private String rutaRedireccion;

        public Constructor tokenAcceso(String tokenAcceso) {
            this.tokenAcceso = tokenAcceso;
            return this;
        }

        public Constructor tipoToken(String tipoToken) {
            this.tipoToken = tipoToken;
            return this;
        }

        public Constructor expiraEnSegundos(long expiraEnSegundos) {
            this.expiraEnSegundos = expiraEnSegundos;
            return this;
        }

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor correoInstitucional(String correoInstitucional) {
            this.correoInstitucional = correoInstitucional;
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

        public Constructor nombrePlan(String nombrePlan) {
            this.nombrePlan = nombrePlan;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor modulosHabilitados(List<String> modulosHabilitados) {
            this.modulosHabilitados = modulosHabilitados;
            return this;
        }

        public Constructor requiereRenovacion(boolean requiereRenovacion) {
            this.requiereRenovacion = requiereRenovacion;
            return this;
        }

        public Constructor rutaRedireccion(String rutaRedireccion) {
            this.rutaRedireccion = rutaRedireccion;
            return this;
        }

        public LoginInstitucionalRespuesta build() {
            return new LoginInstitucionalRespuesta(tokenAcceso, tipoToken, expiraEnSegundos, idInstitucion,
                    razonSocial, correoInstitucional, rol, codigoPlan, nombrePlan, estadoSuscripcion,
                    modulosHabilitados, requiereRenovacion, rutaRedireccion);
        }
    }
}
