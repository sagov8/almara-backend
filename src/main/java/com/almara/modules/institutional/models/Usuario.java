package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

/**
 * Superclase abstracta de identidad y autenticación en Almara.
 * Representa la generalización base según el diagrama de clases de dominio.
 */
@MappedSuperclass
public abstract class Usuario {

    @Id
    @Column(name = "id_usuario")
    protected UUID idUsuario;

    @Column(name = "correo", length = 100, nullable = false, unique = true)
    protected String correo;

    @Column(name = "password_hash", length = 255, nullable = false)
    protected String passwordHash;

    @Column(name = "activo", nullable = false)
    protected boolean activo = true;

    public Usuario() {
    }

    public Usuario(UUID idUsuario, String correo, String passwordHash, boolean activo) {
        this.idUsuario = idUsuario;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.activo = activo;
    }

    public abstract boolean autenticar(String passwordPlano);

    public void cerrarSesion(String token) {
        // Método de ciclo de vida de sesión
    }

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
