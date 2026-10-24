package com.almara.modules.institutional.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO que expone la matriz de permisos de módulos para la suscripción actual del cliente institucional (HU-20).
 */
public class MatrizPermisosRespuesta {

    private UUID idInstitucion;
    private UUID idSuscripcion;
    private String codigoPlan;
    private String nombrePlan;
    private String estadoSuscripcion;
    private Instant fechaExpiracion;
    private boolean suscripcionVigente;
    private List<ModuloCatalogoDto> modulos = new ArrayList<>();

    public MatrizPermisosRespuesta() {
    }

    public MatrizPermisosRespuesta(UUID idInstitucion, UUID idSuscripcion, String codigoPlan,
                                  String nombrePlan, String estadoSuscripcion, Instant fechaExpiracion,
                                  boolean suscripcionVigente, List<ModuloCatalogoDto> modulos) {
        this.idInstitucion = idInstitucion;
        this.idSuscripcion = idSuscripcion;
        this.codigoPlan = codigoPlan;
        this.nombrePlan = nombrePlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.fechaExpiracion = fechaExpiracion;
        this.suscripcionVigente = suscripcionVigente;
        this.modulos = modulos != null ? modulos : new ArrayList<>();
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public UUID getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(UUID idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
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

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Instant fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isSuscripcionVigente() {
        return suscripcionVigente;
    }

    public void setSuscripcionVigente(boolean suscripcionVigente) {
        this.suscripcionVigente = suscripcionVigente;
    }

    public List<ModuloCatalogoDto> getModulos() {
        return modulos;
    }

    public void setModulos(List<ModuloCatalogoDto> modulos) {
        this.modulos = modulos;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idInstitucion;
        private UUID idSuscripcion;
        private String codigoPlan;
        private String nombrePlan;
        private String estadoSuscripcion;
        private Instant fechaExpiracion;
        private boolean suscripcionVigente;
        private List<ModuloCatalogoDto> modulos = new ArrayList<>();

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor idSuscripcion(UUID idSuscripcion) {
            this.idSuscripcion = idSuscripcion;
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

        public Constructor fechaExpiracion(Instant fechaExpiracion) {
            this.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public Constructor suscripcionVigente(boolean suscripcionVigente) {
            this.suscripcionVigente = suscripcionVigente;
            return this;
        }

        public Constructor modulos(List<ModuloCatalogoDto> modulos) {
            this.modulos = modulos;
            return this;
        }

        public MatrizPermisosRespuesta build() {
            return new MatrizPermisosRespuesta(idInstitucion, idSuscripcion, codigoPlan, nombrePlan,
                    estadoSuscripcion, fechaExpiracion, suscripcionVigente, modulos);
        }
    }
}
