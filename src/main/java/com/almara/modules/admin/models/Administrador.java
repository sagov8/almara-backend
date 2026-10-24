package com.almara.modules.admin.models;

import com.almara.modules.institutional.models.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad Administrador del Sistema Almara.
 * Representa al actor primario del Módulo de Administración (HU-11 a HU-15, HU-18).
 * Extiende la superclase abstracta de identidad Usuario.
 */
@Entity
@Table(name = "administrador")
public class Administrador extends Usuario {

    @Column(name = "nombre_completo", length = 120, nullable = false)
    private String nombreCompleto;

    @Column(name = "rol_nombre", length = 40, nullable = false)
    private String rolNombre = "ROLE_ADMIN";

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Administrador() {
        super();
    }

    public Administrador(UUID idUsuario, String correo, String passwordHash, boolean activo,
                         String nombreCompleto, String rolNombre) {
        super(idUsuario, correo, passwordHash, activo);
        this.nombreCompleto = nombreCompleto;
        this.rolNombre = rolNombre != null ? rolNombre : "ROLE_ADMIN";
        this.intentosFallidos = 0;
        this.fechaCreacion = LocalDateTime.now();
    }

    @Override
    public boolean autenticar(String passwordPlano) {
        // La autenticación formal se delega a la estrategia de hashing inyectada en el servicio
        return this.activo && !estaBloqueado();
    }

    /**
     * Determina si la cuenta administrativa se encuentra temporalmente bloqueada por exceso de intentos fallidos.
     */
    public boolean estaBloqueado() {
        if (this.bloqueadoHasta == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.bloqueadoHasta);
    }

    /**
     * Registra un intento fallido de autenticación. Si alcanza el límite, activa el bloqueo temporal.
     *
     * @param maxIntentos     Cantidad máxima de intentos permitidos antes de bloquear (ej. 5)
     * @param minutosBloqueo  Duración del bloqueo temporal en minutos (ej. 15)
     * @return true si la cuenta quedó bloqueada a raíz de este intento
     */
    public boolean registrarIntentoFallido(int maxIntentos, int minutosBloqueo) {
        this.intentosFallidos++;
        if (this.intentosFallidos >= maxIntentos) {
            this.bloqueadoHasta = LocalDateTime.now().plusMinutes(minutosBloqueo);
            return true;
        }
        return false;
    }

    /**
     * Restablece los contadores de intentos fallidos y anula cualquier bloqueo previo.
     */
    public void restablecerIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    /**
     * Registra un inicio de sesión exitoso, actualizando la fecha y restableciendo intentos.
     */
    public void registrarLoginExitoso() {
        this.ultimoLogin = LocalDateTime.now();
        restablecerIntentosFallidos();
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
