package com.almara.modules.institutional.models;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Superclase abstracta de dominio para el catálogo comercial (Módulo 2).
 */
@MappedSuperclass
public abstract class ProductoServicio {

    @Id
    protected UUID idProducto;

    protected String codigo;

    protected String nombre;

    protected String descripcion;

    protected BigDecimal precioBase;

    public ProductoServicio() {
    }

    public ProductoServicio(UUID idProducto, String codigo, String nombre, String descripcion, BigDecimal precioBase) {
        this.idProducto = idProducto;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioBase = precioBase;
    }

    public abstract BigDecimal calcularPrecioFinal();

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
}
