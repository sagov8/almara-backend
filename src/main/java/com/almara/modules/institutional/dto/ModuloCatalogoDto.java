package com.almara.modules.institutional.dto;

import java.util.UUID;

/**
 * DTO que describe un módulo del sistema y su estado de habilitación dentro de un plan SaaS.
 */
public class ModuloCatalogoDto {

    private UUID idModulo;
    private String codigoModulo;
    private String nombreModulo;
    private String descripcion;
    private boolean esPremium;
    private boolean habilitadoEnPlan;
    private String endpointRestringido;

    public ModuloCatalogoDto() {
    }

    public ModuloCatalogoDto(UUID idModulo, String codigoModulo, String nombreModulo,
                             String descripcion, boolean esPremium, boolean habilitadoEnPlan,
                             String endpointRestringido) {
        this.idModulo = idModulo;
        this.codigoModulo = codigoModulo;
        this.nombreModulo = nombreModulo;
        this.descripcion = descripcion;
        this.esPremium = esPremium;
        this.habilitadoEnPlan = habilitadoEnPlan;
        this.endpointRestringido = endpointRestringido;
    }

    public UUID getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(UUID idModulo) {
        this.idModulo = idModulo;
    }

    public String getCodigoModulo() {
        return codigoModulo;
    }

    public void setCodigoModulo(String codigoModulo) {
        this.codigoModulo = codigoModulo;
    }

    public String getNombreModulo() {
        return nombreModulo;
    }

    public void setNombreModulo(String nombreModulo) {
        this.nombreModulo = nombreModulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsPremium() {
        return esPremium;
    }

    public void setEsPremium(boolean esPremium) {
        this.esPremium = esPremium;
    }

    public boolean isHabilitadoEnPlan() {
        return habilitadoEnPlan;
    }

    public void setHabilitadoEnPlan(boolean habilitadoEnPlan) {
        this.habilitadoEnPlan = habilitadoEnPlan;
    }

    public String getEndpointRestringido() {
        return endpointRestringido;
    }

    public void setEndpointRestringido(String endpointRestringido) {
        this.endpointRestringido = endpointRestringido;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idModulo;
        private String codigoModulo;
        private String nombreModulo;
        private String descripcion;
        private boolean esPremium;
        private boolean habilitadoEnPlan;
        private String endpointRestringido;

        public Constructor idModulo(UUID idModulo) {
            this.idModulo = idModulo;
            return this;
        }

        public Constructor codigoModulo(String codigoModulo) {
            this.codigoModulo = codigoModulo;
            return this;
        }

        public Constructor nombreModulo(String nombreModulo) {
            this.nombreModulo = nombreModulo;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Constructor esPremium(boolean esPremium) {
            this.esPremium = esPremium;
            return this;
        }

        public Constructor habilitadoEnPlan(boolean habilitadoEnPlan) {
            this.habilitadoEnPlan = habilitadoEnPlan;
            return this;
        }

        public Constructor endpointRestringido(String endpointRestringido) {
            this.endpointRestringido = endpointRestringido;
            return this;
        }

        public ModuloCatalogoDto build() {
            return new ModuloCatalogoDto(idModulo, codigoModulo, nombreModulo, descripcion, esPremium, habilitadoEnPlan, endpointRestringido);
        }
    }
}
