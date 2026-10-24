package com.almara.modules.institutional.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO que encapsula el resumen analítico general para el dashboard de clientes institucionales (HU-24).
 * Integra los flags dinámicos de acceso a módulos según el plan SaaS activo (HU-20).
 */
public class DashboardInstitucionalResumenRespuesta {

    private UUID idInstitucion;
    private String razonSocial;
    private String codigoPlanActivo;
    private String periodoConsultado;
    private int totalZonasMonitoreadas;
    private int zonasConDatosSuficientes;
    private int zonasEnReservaPrivacidad;
    private String emocionPredominanteCiudad;
    private double intensidadPromedioCiudad;
    private List<IndicadorEmocionCiudadDto> distribucionGlobalCiudad;
    private List<ZonaTerritorialAnaliticaDto> resumenZonas;
    private boolean filtroTerritorialHabilitado;
    private boolean exportacionHabilitada;
    private long tiempoCalculoMs;

    public DashboardInstitucionalResumenRespuesta() {
    }

    public DashboardInstitucionalResumenRespuesta(UUID idInstitucion, String razonSocial, String codigoPlanActivo,
                                                  String periodoConsultado, int totalZonasMonitoreadas,
                                                  int zonasConDatosSuficientes, int zonasEnReservaPrivacidad,
                                                  String emocionPredominanteCiudad, double intensidadPromedioCiudad,
                                                  List<IndicadorEmocionCiudadDto> distribucionGlobalCiudad,
                                                  List<ZonaTerritorialAnaliticaDto> resumenZonas,
                                                  boolean filtroTerritorialHabilitado, boolean exportacionHabilitada,
                                                  long tiempoCalculoMs) {
        this.idInstitucion = idInstitucion;
        this.razonSocial = razonSocial;
        this.codigoPlanActivo = codigoPlanActivo;
        this.periodoConsultado = periodoConsultado;
        this.totalZonasMonitoreadas = totalZonasMonitoreadas;
        this.zonasConDatosSuficientes = zonasConDatosSuficientes;
        this.zonasEnReservaPrivacidad = zonasEnReservaPrivacidad;
        this.emocionPredominanteCiudad = emocionPredominanteCiudad;
        this.intensidadPromedioCiudad = intensidadPromedioCiudad;
        this.distribucionGlobalCiudad = distribucionGlobalCiudad;
        this.resumenZonas = resumenZonas;
        this.filtroTerritorialHabilitado = filtroTerritorialHabilitado;
        this.exportacionHabilitada = exportacionHabilitada;
        this.tiempoCalculoMs = tiempoCalculoMs;
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

    public String getCodigoPlanActivo() {
        return codigoPlanActivo;
    }

    public void setCodigoPlanActivo(String codigoPlanActivo) {
        this.codigoPlanActivo = codigoPlanActivo;
    }

    public String getPeriodoConsultado() {
        return periodoConsultado;
    }

    public void setPeriodoConsultado(String periodoConsultado) {
        this.periodoConsultado = periodoConsultado;
    }

    public int getTotalZonasMonitoreadas() {
        return totalZonasMonitoreadas;
    }

    public void setTotalZonasMonitoreadas(int totalZonasMonitoreadas) {
        this.totalZonasMonitoreadas = totalZonasMonitoreadas;
    }

    public int getZonasConDatosSuficientes() {
        return zonasConDatosSuficientes;
    }

    public void setZonasConDatosSuficientes(int zonasConDatosSuficientes) {
        this.zonasConDatosSuficientes = zonasConDatosSuficientes;
    }

    public int getZonasEnReservaPrivacidad() {
        return zonasEnReservaPrivacidad;
    }

    public void setZonasEnReservaPrivacidad(int zonasEnReservaPrivacidad) {
        this.zonasEnReservaPrivacidad = zonasEnReservaPrivacidad;
    }

    public String getEmocionPredominanteCiudad() {
        return emocionPredominanteCiudad;
    }

    public void setEmocionPredominanteCiudad(String emocionPredominanteCiudad) {
        this.emocionPredominanteCiudad = emocionPredominanteCiudad;
    }

    public double getIntensidadPromedioCiudad() {
        return intensidadPromedioCiudad;
    }

    public void setIntensidadPromedioCiudad(double intensidadPromedioCiudad) {
        this.intensidadPromedioCiudad = intensidadPromedioCiudad;
    }

    public List<IndicadorEmocionCiudadDto> getDistribucionGlobalCiudad() {
        return distribucionGlobalCiudad;
    }

    public void setDistribucionGlobalCiudad(List<IndicadorEmocionCiudadDto> distribucionGlobalCiudad) {
        this.distribucionGlobalCiudad = distribucionGlobalCiudad;
    }

    public List<ZonaTerritorialAnaliticaDto> getResumenZonas() {
        return resumenZonas;
    }

    public void setResumenZonas(List<ZonaTerritorialAnaliticaDto> resumenZonas) {
        this.resumenZonas = resumenZonas;
    }

    public boolean isFiltroTerritorialHabilitado() {
        return filtroTerritorialHabilitado;
    }

    public void setFiltroTerritorialHabilitado(boolean filtroTerritorialHabilitado) {
        this.filtroTerritorialHabilitado = filtroTerritorialHabilitado;
    }

    public boolean isExportacionHabilitada() {
        return exportacionHabilitada;
    }

    public void setExportacionHabilitada(boolean exportacionHabilitada) {
        this.exportacionHabilitada = exportacionHabilitada;
    }

    public long getTiempoCalculoMs() {
        return tiempoCalculoMs;
    }

    public void setTiempoCalculoMs(long tiempoCalculoMs) {
        this.tiempoCalculoMs = tiempoCalculoMs;
    }

    public static class Constructor {
        private UUID idInstitucion;
        private String razonSocial;
        private String codigoPlanActivo;
        private String periodoConsultado;
        private int totalZonasMonitoreadas;
        private int zonasConDatosSuficientes;
        private int zonasEnReservaPrivacidad;
        private String emocionPredominanteCiudad;
        private double intensidadPromedioCiudad;
        private List<IndicadorEmocionCiudadDto> distribucionGlobalCiudad;
        private List<ZonaTerritorialAnaliticaDto> resumenZonas;
        private boolean filtroTerritorialHabilitado;
        private boolean exportacionHabilitada;
        private long tiempoCalculoMs;

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor codigoPlanActivo(String codigoPlanActivo) {
            this.codigoPlanActivo = codigoPlanActivo;
            return this;
        }

        public Constructor periodoConsultado(String periodoConsultado) {
            this.periodoConsultado = periodoConsultado;
            return this;
        }

        public Constructor totalZonasMonitoreadas(int totalZonasMonitoreadas) {
            this.totalZonasMonitoreadas = totalZonasMonitoreadas;
            return this;
        }

        public Constructor zonasConDatosSuficientes(int zonasConDatosSuficientes) {
            this.zonasConDatosSuficientes = zonasConDatosSuficientes;
            return this;
        }

        public Constructor zonasEnReservaPrivacidad(int zonasEnReservaPrivacidad) {
            this.zonasEnReservaPrivacidad = zonasEnReservaPrivacidad;
            return this;
        }

        public Constructor emocionPredominanteCiudad(String emocionPredominanteCiudad) {
            this.emocionPredominanteCiudad = emocionPredominanteCiudad;
            return this;
        }

        public Constructor intensidadPromedioCiudad(double intensidadPromedioCiudad) {
            this.intensidadPromedioCiudad = intensidadPromedioCiudad;
            return this;
        }

        public Constructor distribucionGlobalCiudad(List<IndicadorEmocionCiudadDto> distribucionGlobalCiudad) {
            this.distribucionGlobalCiudad = distribucionGlobalCiudad;
            return this;
        }

        public Constructor resumenZonas(List<ZonaTerritorialAnaliticaDto> resumenZonas) {
            this.resumenZonas = resumenZonas;
            return this;
        }

        public Constructor filtroTerritorialHabilitado(boolean filtroTerritorialHabilitado) {
            this.filtroTerritorialHabilitado = filtroTerritorialHabilitado;
            return this;
        }

        public Constructor exportacionHabilitada(boolean exportacionHabilitada) {
            this.exportacionHabilitada = exportacionHabilitada;
            return this;
        }

        public Constructor tiempoCalculoMs(long tiempoCalculoMs) {
            this.tiempoCalculoMs = tiempoCalculoMs;
            return this;
        }

        public DashboardInstitucionalResumenRespuesta build() {
            return new DashboardInstitucionalResumenRespuesta(
                    idInstitucion, razonSocial, codigoPlanActivo, periodoConsultado,
                    totalZonasMonitoreadas, zonasConDatosSuficientes, zonasEnReservaPrivacidad,
                    emocionPredominanteCiudad, intensidadPromedioCiudad, distribucionGlobalCiudad,
                    resumenZonas, filtroTerritorialHabilitado, exportacionHabilitada, tiempoCalculoMs
            );
        }
    }
}
