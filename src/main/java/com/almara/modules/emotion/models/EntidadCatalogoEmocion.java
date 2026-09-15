package com.almara.modules.emotion.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA mapeada a la tabla catalogo_emocion del modelo de datos de Almara (almara_dbdiagram.dbml).
 * Representa el catálogo oficial de emociones administrables (HU-12).
 */
@Entity
@Table(name = "catalogo_emocion")
public class EntidadCatalogoEmocion {

    @Id
    @Column(name = "id_emocion", length = 20, nullable = false)
    private String idEmocion;

    @Column(name = "nombre_etiqueta", length = 30, nullable = false)
    private String nombreEtiqueta;

    @Column(name = "codigo_hex_color", length = 7, nullable = false)
    private String codigoHexColor;

    @Column(name = "codigo_hex_fondo", length = 7)
    private String codigoHexFondo;

    @Column(name = "icono_svg", length = 255)
    private String iconoSvg;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    public EntidadCatalogoEmocion() {
    }

    public EntidadCatalogoEmocion(String idEmocion, String nombreEtiqueta, String codigoHexColor,
                                  String codigoHexFondo, String iconoSvg, String descripcion,
                                  Boolean activo, Instant fechaCreacion) {
        this.idEmocion = idEmocion;
        this.nombreEtiqueta = nombreEtiqueta;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.iconoSvg = iconoSvg;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getIdEmocion() {
        return idEmocion;
    }

    public void setIdEmocion(String idEmocion) {
        this.idEmocion = idEmocion;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = codigoHexColor;
    }

    public String getCodigoHexFondo() {
        return codigoHexFondo;
    }

    public void setCodigoHexFondo(String codigoHexFondo) {
        this.codigoHexFondo = codigoHexFondo;
    }

    public String getIconoSvg() {
        return iconoSvg;
    }

    public void setIconoSvg(String iconoSvg) {
        this.iconoSvg = iconoSvg;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public static class Constructor {
        private String idEmocion;
        private String nombreEtiqueta;
        private String codigoHexColor;
        private String codigoHexFondo;
        private String iconoSvg;
        private String descripcion;
        private Boolean activo = true;
        private Instant fechaCreacion = Instant.now();

        public Constructor idEmocion(String idEmocion) {
            this.idEmocion = idEmocion;
            return this;
        }

        public Constructor nombreEtiqueta(String nombreEtiqueta) {
            this.nombreEtiqueta = nombreEtiqueta;
            return this;
        }

        public Constructor codigoHexColor(String codigoHexColor) {
            this.codigoHexColor = codigoHexColor;
            return this;
        }

        public Constructor codigoHexFondo(String codigoHexFondo) {
            this.codigoHexFondo = codigoHexFondo;
            return this;
        }

        public Constructor iconoSvg(String iconoSvg) {
            this.iconoSvg = iconoSvg;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Constructor activo(Boolean activo) {
            this.activo = activo;
            return this;
        }

        public Constructor fechaCreacion(Instant fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public EntidadCatalogoEmocion build() {
            return new EntidadCatalogoEmocion(idEmocion, nombreEtiqueta, codigoHexColor, codigoHexFondo, iconoSvg, descripcion, activo, fechaCreacion);
        }
    }
}
