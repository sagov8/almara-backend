package com.almara.modules.institutional.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO que describe el estado integral de la suscripción SaaS actual del cliente institucional (HU-21).
 */
public class EstadoSuscripcionRespuesta {

    private UUID idSuscripcion;
    private UUID idInstitucion;
    private String razonSocial;
    private String codigoPlan;
    private String nombrePlan;
    private BigDecimal precioPlan;
    private boolean esGratuito;
    private String estadoSuscripcion;
    private boolean vigente;
    private Instant fechaInicio;
    private Instant fechaExpiracion;
    private long diasRestantes;
    private boolean alertaVencimiento;
    private String mensajeEstado;
    private List<String> modulosHabilitados = new ArrayList<>();
    private List<String> modulosRestringidos = new ArrayList<>();
    private List<ResumenOrdenSuscripcionDto> historialOrdenes = new ArrayList<>();

    public EstadoSuscripcionRespuesta() {
    }

    public EstadoSuscripcionRespuesta(UUID idSuscripcion, UUID idInstitucion, String razonSocial,
                                      String codigoPlan, String nombrePlan, BigDecimal precioPlan,
                                      boolean esGratuito, String estadoSuscripcion, boolean vigente,
                                      Instant fechaInicio, Instant fechaExpiracion, long diasRestantes,
                                      boolean alertaVencimiento, String mensajeEstado,
                                      List<String> modulosHabilitados, List<String> modulosRestringidos,
                                      List<ResumenOrdenSuscripcionDto> historialOrdenes) {
        this.idSuscripcion = idSuscripcion;
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.codigoPlan = codigoPlan;
        this.nombrePlan = nombrePlan;
        this.precioPlan = precioPlan;
        this.esGratuito = esGratuito;
        this.estadoSuscripcion = estadoSuscripcion;
        this.vigente = vigente;
        this.fechaInicio = fechaInicio;
        this.fechaExpiracion = fechaExpiracion;
        this.diasRestantes = diasRestantes;
        this.alertaVencimiento = alertaVencimiento;
        this.mensajeEstado = mensajeEstado;
        this.modulosHabilitados = modulosHabilitados != null ? modulosHabilitados : new ArrayList<>();
        this.modulosRestringidos = modulosRestringidos != null ? modulosRestringidos : new ArrayList<>();
        this.historialOrdenes = historialOrdenes != null ? historialOrdenes : new ArrayList<>();
    }

    public UUID getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(UUID idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public BigDecimal getPrecioPlan() {
        return precioPlan;
    }

    public void setPrecioPlan(BigDecimal precioPlan) {
        this.precioPlan = precioPlan;
    }

    public boolean isEsGratuito() {
        return esGratuito;
    }

    public void setEsGratuito(boolean esGratuito) {
        this.esGratuito = esGratuito;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public boolean isVigente() {
        return vigente;
    }

    public void setVigente(boolean vigente) {
        this.vigente = vigente;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(Instant fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public long getDiasRestantes() {
        return diasRestantes;
    }

    public void setDiasRestantes(long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public boolean isAlertaVencimiento() {
        return alertaVencimiento;
    }

    public void setAlertaVencimiento(boolean alertaVencimiento) {
        this.alertaVencimiento = alertaVencimiento;
    }

    public String getMensajeEstado() {
        return mensajeEstado;
    }

    public void setMensajeEstado(String mensajeEstado) {
        this.mensajeEstado = mensajeEstado;
    }

    public List<String> getModulosHabilitados() {
        return modulosHabilitados;
    }

    public void setModulosHabilitados(List<String> modulosHabilitados) {
        this.modulosHabilitados = modulosHabilitados;
    }

    public List<String> getModulosRestringidos() {
        return modulosRestringidos;
    }

    public void setModulosRestringidos(List<String> modulosRestringidos) {
        this.modulosRestringidos = modulosRestringidos;
    }

    public List<ResumenOrdenSuscripcionDto> getHistorialOrdenes() {
        return historialOrdenes;
    }

    public void setHistorialOrdenes(List<ResumenOrdenSuscripcionDto> historialOrdenes) {
        this.historialOrdenes = historialOrdenes;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idSuscripcion;
        private UUID idInstitucion;
        private String razonSocial;
        private String codigoPlan;
        private String nombrePlan;
        private BigDecimal precioPlan;
        private boolean esGratuito;
        private String estadoSuscripcion;
        private boolean vigente;
        private Instant fechaInicio;
        private Instant fechaExpiracion;
        private long diasRestantes;
        private boolean alertaVencimiento;
        private String mensajeEstado;
        private List<String> modulosHabilitados = new ArrayList<>();
        private List<String> modulosRestringidos = new ArrayList<>();
        private List<ResumenOrdenSuscripcionDto> historialOrdenes = new ArrayList<>();

        public Constructor idSuscripcion(UUID idSuscripcion) {
            this.idSuscripcion = idSuscripcion;
            return this;
        }

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor nombrePlan(String nombrePlan) {
            this.nombrePlan = nombrePlan;
            return this;
        }

        public Constructor precioPlan(BigDecimal precioPlan) {
            this.precioPlan = precioPlan;
            return this;
        }

        public Constructor esGratuito(boolean esGratuito) {
            this.esGratuito = esGratuito;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor vigente(boolean vigente) {
            this.vigente = vigente;
            return this;
        }

        public Constructor fechaInicio(Instant fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public Constructor fechaExpiracion(Instant fechaExpiracion) {
            this.fechaExpiracion = fechaExpiracion;
            return this;
        }

        public Constructor diasRestantes(long diasRestantes) {
            this.diasRestantes = diasRestantes;
            return this;
        }

        public Constructor alertaVencimiento(boolean alertaVencimiento) {
            this.alertaVencimiento = alertaVencimiento;
            return this;
        }

        public Constructor mensajeEstado(String mensajeEstado) {
            this.mensajeEstado = mensajeEstado;
            return this;
        }

        public Constructor modulosHabilitados(List<String> modulosHabilitados) {
            this.modulosHabilitados = modulosHabilitados;
            return this;
        }

        public Constructor modulosRestringidos(List<String> modulosRestringidos) {
            this.modulosRestringidos = modulosRestringidos;
            return this;
        }

        public Constructor historialOrdenes(List<ResumenOrdenSuscripcionDto> historialOrdenes) {
            this.historialOrdenes = historialOrdenes;
            return this;
        }

        public EstadoSuscripcionRespuesta build() {
            return new EstadoSuscripcionRespuesta(idSuscripcion, idInstitucion, razonSocial, codigoPlan,
                    nombrePlan, precioPlan, esGratuito, estadoSuscripcion, vigente, fechaInicio,
                    fechaExpiracion, diasRestantes, alertaVencimiento, mensajeEstado,
                    modulosHabilitados, modulosRestringidos, historialOrdenes);
        }
    }
}
