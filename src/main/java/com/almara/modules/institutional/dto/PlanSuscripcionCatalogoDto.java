package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO que representa un Plan de Suscripción SaaS para el catálogo comercial (HU-19).
 */
public class PlanSuscripcionCatalogoDto {

    private UUID idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private BigDecimal precioFinal;
    private String tipoPlan;
    private int duracionDias;
    private boolean esGratuito;
    private boolean esPlanActual;
    private List<ModuloCatalogoDto> modulos = new ArrayList<>();

    public PlanSuscripcionCatalogoDto() {
    }

    public PlanSuscripcionCatalogoDto(UUID idProducto, String codigo, String nombre,
                                      String descripcion, BigDecimal precioBase, BigDecimal precioFinal,
                                      String tipoPlan, int duracionDias, boolean esGratuito,
                                      boolean esPlanActual, List<ModuloCatalogoDto> modulos) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
        this.precioFinal = precioFinal;
        this.tipoPlan = tipoPlan;
        this.duracionDias = duracionDias;
        this.esGratuito = esGratuito;
        this.esPlanActual = esPlanActual;
        this.modulos = modulos != null ? modulos : new ArrayList<>();
    }

    public UUID getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(UUID idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(BigDecimal precioFinal) {
        this.precioFinal = precioFinal;
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

    public boolean isEsPlanActual() {
        return esPlanActual;
    }

    public void setEsPlanActual(boolean esPlanActual) {
        this.esPlanActual = esPlanActual;
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
        private UUID idProducto;
        private String codigo;
        private String nombre;
        private String descripcion;
        private BigDecimal precioBase;
        private BigDecimal precioFinal;
        private String tipoPlan;
        private int duracionDias;
        private boolean esGratuito;
        private boolean esPlanActual;
        private List<ModuloCatalogoDto> modulos = new ArrayList<>();

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

        public Constructor precioFinal(BigDecimal precioFinal) {
            this.precioFinal = precioFinal;
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

        public Constructor esPlanActual(boolean esPlanActual) {
            this.esPlanActual = esPlanActual;
            return this;
        }

        public Constructor modulos(List<ModuloCatalogoDto> modulos) {
            this.modulos = modulos;
            return this;
        }

        public PlanSuscripcionCatalogoDto build() {
            return new PlanSuscripcionCatalogoDto(idProducto, codigo, nombre, descripcion, precioBase, precioFinal, tipoPlan, duracionDias, esGratuito, esPlanActual, modulos);
        }
    }
}
