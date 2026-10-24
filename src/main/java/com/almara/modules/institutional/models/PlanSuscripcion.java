package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Entidad de dominio Plan de Suscripción SaaS (HU-19, HU-20).
 * Hereda de ProductoServicio.
 */
@Entity
@Table(name = "plan_suscripcion")
public class PlanSuscripcion extends ProductoServicio {

    @Column(name = "tipo_plan", length = 30, nullable = false)
    private String tipoPlan;

    @Column(name = "duracion_dias", nullable = false)
    private int duracionDias;

    @Column(name = "es_gratuito", nullable = false)
    private boolean esGratuito;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public PlanSuscripcion() {
        super();
    }

    public PlanSuscripcion(UUID idProducto, String codigo, String nombre, String descripcion,
                           BigDecimal precioBase, String tipoPlan, int duracionDias,
                           boolean esGratuito, boolean activo) {
        super(idProducto, codigo, nombre, descripcion, precioBase);
        this.tipoPlan = tipoPlan;
        this.duracionDias = duracionDias;
        this.esGratuito = esGratuito;
        this.activo = activo;
    }

    @Override
    public BigDecimal calcularPrecioFinal() {
        if (this.esGratuito) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return this.precioBase != null ? this.precioBase.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean esCompatibleConModulo(UUID idModulo, java.util.List<MatrizPermisoModulo> matriz) {
        if (idModulo == null || matriz == null) {
            return false;
        }
        return matriz.stream()
                .anyMatch(m -> this.getIdProducto().equals(m.getIdPlan())
                        && idModulo.equals(m.getIdModulo())
                        && m.isModuloHabilitado());
    }

    public String getTipoPlan() {
        return tipoPlan;
    }

    public void setTipoPlan(String tipoPlan) {
        this.tipoPlan = tipoPlan;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(int duracionDias) {
        this.duracionDias = duracionDias;
    }

    public boolean isEsGratuito() {
        return esGratuito;
    }

    public void setEsGratuito(boolean esGratuito) {
        this.esGratuito = esGratuito;
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
        private UUID idProducto;
        private String codigo;
        private String nombre;
        private String descripcion;
        private BigDecimal precioBase = BigDecimal.ZERO;
        private String tipoPlan = "FREEMIUM";
        private int duracionDias = 30;
        private boolean esGratuito = false;
        private boolean activo = true;

        public Constructor idProducto(UUID idProducto) {
            this.idProducto = idProducto;
            return this;
        }

        public Constructor codigo(String codigo) {
            this.codigo = codigo;
            return this;
        }

        public Constructor nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Constructor precioBase(BigDecimal precioBase) {
            this.precioBase = precioBase;
            return this;
        }

        public Constructor tipoPlan(String tipoPlan) {
            this.tipoPlan = tipoPlan;
            return this;
        }

        public Constructor duracionDias(int duracionDias) {
            this.duracionDias = duracionDias;
            return this;
        }

        public Constructor esGratuito(boolean esGratuito) {
            this.esGratuito = esGratuito;
            return this;
        }

        public Constructor activo(boolean activo) {
            this.activo = activo;
            return this;
        }

        public PlanSuscripcion build() {
            return new PlanSuscripcion(idProducto, codigo, nombre, descripcion, precioBase, tipoPlan, duracionDias, esGratuito, activo);
        }
    }
}
