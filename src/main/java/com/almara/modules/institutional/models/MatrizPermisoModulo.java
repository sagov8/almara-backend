package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Matriz de Permiso entre Plan de Suscripción y Módulo del Sistema (HU-20).
 */
@Entity
@Table(name = "matriz_permiso_modulo")
public class MatrizPermisoModulo {

    @Id
    @Column(name = "id_matriz")
    private UUID idMatriz;

    @Column(name = "id_plan", nullable = false)
    private UUID idPlan;

    @Column(name = "id_modulo", nullable = false)
    private UUID idModulo;

    @Column(name = "modulo_habilitado", nullable = false)
    private boolean moduloHabilitado = true;

    @Column(name = "endpoint_restringido", length = 150, nullable = false)
    private String endpointRestringido;

    public MatrizPermisoModulo() {
    }

    public MatrizPermisoModulo(UUID idMatriz, UUID idPlan, UUID idModulo, boolean moduloHabilitado, String endpointRestringido) {
        this.idMatriz = idMatriz;
        this.idPlan = idPlan;
        this.idModulo = idModulo;
        this.moduloHabilitado = moduloHabilitado;
        this.endpointRestringido = endpointRestringido;
    }

    public boolean validarAutorizacion() {
        return moduloHabilitado;
    }

    public UUID getIdMatriz() {
        return idMatriz;
    }

    public void setIdMatriz(UUID idMatriz) {
        this.idMatriz = idMatriz;
    }

    public UUID getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(UUID idPlan) {
        this.idPlan = idPlan;
    }

    public UUID getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(UUID idModulo) {
        this.idModulo = idModulo;
    }

    public boolean isModuloHabilitado() {
        return moduloHabilitado;
    }

    public void setModuloHabilitado(boolean moduloHabilitado) {
        this.moduloHabilitado = moduloHabilitado;
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
        private UUID idMatriz;
        private UUID idPlan;
        private UUID idModulo;
        private boolean moduloHabilitado = true;
        private String endpointRestringido;

        public Constructor idMatriz(UUID idMatriz) {
            this.idMatriz = idMatriz;
            return this;
        }

        public Constructor idPlan(UUID idPlan) {
            this.idPlan = idPlan;
            return this;
        }

        public Constructor idModulo(UUID idModulo) {
            this.idModulo = idModulo;
            return this;
        }

        public Constructor moduloHabilitado(boolean moduloHabilitado) {
            this.moduloHabilitado = moduloHabilitado;
            return this;
        }

        public Constructor endpointRestringido(String endpointRestringido) {
            this.endpointRestringido = endpointRestringido;
            return this;
        }

        public MatrizPermisoModulo build() {
            return new MatrizPermisoModulo(idMatriz, idPlan, idModulo, moduloHabilitado, endpointRestringido);
        }
    }
}
