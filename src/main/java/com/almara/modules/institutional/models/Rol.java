package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidad Rol del Sistema para control de acceso RBAC.
 */
@Entity
@Table(name = "rol_sistema")
public class Rol {

    @Id
    @Column(name = "id_rol")
    private UUID idRol;

    @Column(name = "nombre_rol", length = 40, nullable = false, unique = true)
    private String nombreRol;

    @Column(name = "descripcion", length = 150)
    private String descripcion;

    public Rol() {
    }

    public Rol(UUID idRol, String nombreRol, String descripcion) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }

    public boolean tienePermisoBase(String accion) {
        if (accion == null || accion.isBlank()) {
            return false;
        }
        return "ROLE_ADMIN".equalsIgnoreCase(this.nombreRol)
                || ("ROLE_INSTITUTIONAL".equalsIgnoreCase(this.nombreRol) && !accion.startsWith("ADMIN_"));
    }

    public UUID getIdRol() {
        return idRol;
    }

    public void setIdRol(UUID idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idRol;
        private String nombreRol;
        private String descripcion;

        public Constructor idRol(UUID idRol) {
            this.idRol = idRol;
            return this;
        }

        public Constructor nombreRol(String nombreRol) {
            this.nombreRol = nombreRol;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Rol build() {
            return new Rol(idRol, nombreRol, descripcion);
        }
    }
}
