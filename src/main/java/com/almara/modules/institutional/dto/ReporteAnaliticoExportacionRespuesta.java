package com.almara.modules.institutional.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO que encapsula el resultado exportable de analítica institucional (HU-24).
 * Disponible exclusivamente para suscriptores con plan PLAN_ENTERPRISE.
 */
public class ReporteAnaliticoExportacionRespuesta {

    private UUID idInstitucion;
    private String razonSocial;
    private String identificacionFiscalNit;
    private String planSuscripcion;
    private String formato;
    private Instant fechaGeneracion;
    private int totalZonasExportadas;
    private String declaracionPrivacidad;
    private List<ZonaTerritorialAnaliticaDto> datosZonas;

    public ReporteAnaliticoExportacionRespuesta() {
    }

    public ReporteAnaliticoExportacionRespuesta(UUID idInstitucion, String razonSocial, String identificacionFiscalNit,
                                                String planSuscripcion, String formato, Instant fechaGeneracion,
                                                int totalZonasExportadas, String declaracionPrivacidad,
                                                List<ZonaTerritorialAnaliticaDto> datosZonas) {
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.identificacionFiscalNit = identificacionFiscalNit;
        this.planSuscripcion = planSuscripcion;
        this.formato = formato;
        this.fechaGeneracion = fechaGeneracion;
        this.totalZonasExportadas = totalZonasExportadas;
        this.declaracionPrivacidad = declaracionPrivacidad;
        this.datosZonas = datosZonas;
    }

    public static Constructor builder() {
        return new Constructor();
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

    public String getIdentificacionFiscalNit() {
        return identificacionFiscalNit;
    }

    public void setIdentificacionFiscalNit(String identificacionFiscalNit) {
        this.identificacionFiscalNit = identificacionFiscalNit;
    }

    public String getPlanSuscripcion() {
        return planSuscripcion;
    }

    public void setPlanSuscripcion(String planSuscripcion) {
        this.planSuscripcion = planSuscripcion;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Instant getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Instant fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getTotalZonasExportadas() {
        return totalZonasExportadas;
    }

    public void setTotalZonasExportadas(int totalZonasExportadas) {
        this.totalZonasExportadas = totalZonasExportadas;
    }

    public String getDeclaracionPrivacidad() {
        return declaracionPrivacidad;
    }

    public void setDeclaracionPrivacidad(String declaracionPrivacidad) {
        this.declaracionPrivacidad = declaracionPrivacidad;
    }

    public List<ZonaTerritorialAnaliticaDto> getDatosZonas() {
        return datosZonas;
    }

    public void setDatosZonas(List<ZonaTerritorialAnaliticaDto> datosZonas) {
        this.datosZonas = datosZonas;
    }

    public static class Constructor {
        private UUID idInstitucion;
        private String razonSocial;
        private String identificacionFiscalNit;
        private String planSuscripcion;
        private String formato;
        private Instant fechaGeneracion;
        private int totalZonasExportadas;
        private String declaracionPrivacidad;
        private List<ZonaTerritorialAnaliticaDto> datosZonas;

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
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

        public Constructor planSuscripcion(String planSuscripcion) {
            this.planSuscripcion = planSuscripcion;
            return this;
        }

        public Constructor formato(String formato) {
            this.formato = formato;
            return this;
        }

        public Constructor fechaGeneracion(Instant fechaGeneracion) {
            this.fechaGeneracion = fechaGeneracion;
            return this;
        }

        public Constructor totalZonasExportadas(int totalZonasExportadas) {
            this.totalZonasExportadas = totalZonasExportadas;
            return this;
        }

        public Constructor declaracionPrivacidad(String declaracionPrivacidad) {
            this.declaracionPrivacidad = declaracionPrivacidad;
            return this;
        }

        public Constructor datosZonas(List<ZonaTerritorialAnaliticaDto> datosZonas) {
            this.datosZonas = datosZonas;
            return this;
        }

        public ReporteAnaliticoExportacionRespuesta build() {
            return new ReporteAnaliticoExportacionRespuesta(
                    idInstitucion, razonSocial, identificacionFiscalNit, planSuscripcion,
                    formato, fechaGeneracion, totalZonasExportadas, declaracionPrivacidad, datosZonas
            );
        }
    }
}
