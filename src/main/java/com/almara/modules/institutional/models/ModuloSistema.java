package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidad Módulo del Sistema para control de licencias y acceso (HU-20).
 */
@Entity
@Table(name = "modulo_sistema")
public class ModuloSistema {

    @Id
    @Column(name = "id_modulo")
    private UUID idModulo;

    @Column(name = "codigo_modulo", length = 50, nullable = false, unique = true)
    private String codigoModulo;

    @Column(name = "nombre_modulo", length = 80, nullable = false)
    private String nombreModulo;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "es_premium", nullable = false)
    private boolean esPremium = false;

    public ModuloSistema() {
    }

    public ModuloSistema(UUID idModulo, String codigoModulo, String nombreModulo, String descripcion, boolean esPremium) {
        this.idModulo = idModulo;
        this.codigoModulo = codigoModulo;
        this.nombreModulo = nombreModulo;
        this.descripcion = descripcion;
        this.esPremium = esPremium;
    }

    public boolean requiereSuscripcionActiva() {
        return esPremium;
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

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idModulo;
        private String codigoModulo;
        private String nombreModulo;
        private String descripcion;
        private boolean esPremium = false;

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

        public ModuloSistema build() {
            return new ModuloSistema(idModulo, codigoModulo, nombreModulo, descripcion, esPremium);
        }
    }
}
