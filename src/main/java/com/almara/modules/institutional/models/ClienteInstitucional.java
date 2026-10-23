package com.almara.modules.institutional.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de dominio Cliente Institucional.
 * Hereda de Usuario e implementa el registro corporativo y ciclo de vida de cuenta.
 */
@Entity
@Table(name = "cliente_institucional")
public class ClienteInstitucional extends Usuario {

    @Column(name = "razon_social", length = 150, nullable = false)
    private String razonSocial;

    @Column(name = "identificacion_fiscal_nit", length = 30, nullable = false, unique = true)
    private String identificacionFiscalNit;

    @Column(name = "nombre_contacto", length = 80, nullable = false)
    private String nombreContacto;

    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Column(name = "sector_actividad", length = 30)
    private String sectorActividad;

    @Column(name = "estado_cuenta", length = 30, nullable = false)
    private String estadoCuenta = EstadoCuentaInstitucional.PENDIENTE_APROBACION.name();

    @Column(name = "correo_verificado", nullable = false)
    private boolean correoVerificado = false;

    @Column(name = "token_verificacion", length = 100)
    private String tokenVerificacion;

    @Column(name = "rol_nombre", length = 40, nullable = false)
    private String rolNombre = "ROLE_INSTITUTIONAL";

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro = Instant.now();

    public ClienteInstitucional() {
        super();
    }

    public ClienteInstitucional(UUID idUsuario, String correo, String passwordHash, boolean activo,
                                String razonSocial, String identificacionFiscalNit, String nombreContacto,
                                String telefonoContacto, String sectorActividad, String estadoCuenta,
                                boolean correoVerificado, String tokenVerificacion, String rolNombre,
                                Instant fechaRegistro) {
        super(idUsuario, correo, passwordHash, activo);
        this.razonSocial = razonSocial;
        this.identificacionFiscalNit = identificacionFiscalNit;
        this.nombreContacto = nombreContacto;
        this.telefonoContacto = telefonoContacto;
        this.sectorActividad = sectorActividad;
        this.estadoCuenta = estadoCuenta != null ? estadoCuenta : EstadoCuentaInstitucional.PENDIENTE_APROBACION.name();
        this.correoVerificado = correoVerificado;
        this.tokenVerificacion = tokenVerificacion;
        this.rolNombre = rolNombre != null ? rolNombre : "ROLE_INSTITUTIONAL";
        this.fechaRegistro = fechaRegistro != null ? fechaRegistro : Instant.now();
    }

    @Override
    public boolean autenticar(String passwordPlano) {
        // La validación real se apoya en UtilidadPasswordHash
        return this.activo && this.correoVerificado && EstadoCuentaInstitucional.ACTIVA.name().equalsIgnoreCase(this.estadoCuenta);
    }

    public void registrarEntidad() {
        this.estadoCuenta = EstadoCuentaInstitucional.PENDIENTE_APROBACION.name();
        this.correoVerificado = false;
        if (this.fechaRegistro == null) {
            this.fechaRegistro = Instant.now();
        }
    }

    public void activarPorVerificacionCorreo() {
        this.correoVerificado = true;
        this.estadoCuenta = EstadoCuentaInstitucional.ACTIVA.name();
        this.tokenVerificacion = null;
    }

    public void accederDashboard() {
        if (!EstadoCuentaInstitucional.ACTIVA.name().equalsIgnoreCase(this.estadoCuenta)) {
            throw new IllegalStateException("La cuenta no está activa para acceder al panel institucional.");
        }
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getIdentificacionFiscalNit() {
        return identificacionFiscalNit;
    }

    public void setIdentificacionFiscalNit(String identificacionFiscalNit) {
        this.identificacionFiscalNit = identificacionFiscalNit;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getSectorActividad() {
        return sectorActividad;
    }

    public void setSectorActividad(String sectorActividad) {
        this.sectorActividad = sectorActividad;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public boolean isCorreoVerificado() {
        return correoVerificado;
    }

    public void setCorreoVerificado(boolean correoVerificado) {
        this.correoVerificado = correoVerificado;
    }

    public String getTokenVerificacion() {
        return tokenVerificacion;
    }

    public void setTokenVerificacion(String tokenVerificacion) {
        this.tokenVerificacion = tokenVerificacion;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idUsuario;
        private String correo;
        private String passwordHash;
        private boolean activo = true;
        private String razonSocial;
        private String identificacionFiscalNit;
        private String nombreContacto;
        private String telefonoContacto;
        private String sectorActividad;
        private String estadoCuenta = EstadoCuentaInstitucional.PENDIENTE_APROBACION.name();
        private boolean correoVerificado = false;
        private String tokenVerificacion;
        private String rolNombre = "ROLE_INSTITUTIONAL";
        private Instant fechaRegistro = Instant.now();

        public Constructor idUsuario(UUID idUsuario) {
            this.idUsuario = idUsuario;
            return this;
        }

        public Constructor correo(String correo) {
            this.correo = correo;
            return this;
        }

        public Constructor passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }

        public Constructor activo(boolean activo) {
            this.activo = activo;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor identificacionFiscalNit(String identificacionFiscalNit) {
            this.identificacionFiscalNit = identificacionFiscalNit;
            return this;
        }

        public Constructor nombreContacto(String nombreContacto) {
            this.nombreContacto = nombreContacto;
            return this;
        }

        public Constructor telefonoContacto(String telefonoContacto) {
            this.telefonoContacto = telefonoContacto;
            return this;
        }

        public Constructor sectorActividad(String sectorActividad) {
            this.sectorActividad = sectorActividad;
            return this;
        }

        public Constructor estadoCuenta(String estadoCuenta) {
            this.estadoCuenta = estadoCuenta;
            return this;
        }

        public Constructor correoVerificado(boolean correoVerificado) {
            this.correoVerificado = correoVerificado;
            return this;
        }

        public Constructor tokenVerificacion(String tokenVerificacion) {
            this.tokenVerificacion = tokenVerificacion;
            return this;
        }

        public Constructor rolNombre(String rolNombre) {
            this.rolNombre = rolNombre;
            return this;
        }

        public Constructor fechaRegistro(Instant fechaRegistro) {
            this.fechaRegistro = fechaRegistro;
            return this;
        }

        public ClienteInstitucional build() {
            return new ClienteInstitucional(idUsuario, correo, passwordHash, activo, razonSocial,
                    identificacionFiscalNit, nombreContacto, telefonoContacto, sectorActividad,
                    estadoCuenta, correoVerificado, tokenVerificacion, rolNombre, fechaRegistro);
        }
    }
}
