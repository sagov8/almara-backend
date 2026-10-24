package com.almara.modules.institutional.dto;

import java.util.List;

/**
 * DTO que encapsula el resultado del filtrado territorial de zonas para clientes institucionales (HU-24).
 * Permite segmentar por zona manual, celda H3, emoción y período.
 */
public class FiltroTerritorialDashboardRespuesta {

    private String zonaFiltrada;
    private String emocionFiltrada;
    private String periodoConsultado;
    private int totalZonasCoincidentes;
    private List<ZonaTerritorialAnaliticaDto> zonasFiltradas;
    private long tiempoCalculoMs;

    public FiltroTerritorialDashboardRespuesta() {
    }

    public FiltroTerritorialDashboardRespuesta(String zonaFiltrada, String emocionFiltrada,
                                               String periodoConsultado, int totalZonasCoincidentes,
                                               List<ZonaTerritorialAnaliticaDto> zonasFiltradas,
                                               long tiempoCalculoMs) {
        this.zonaFiltrada = zonaFiltrada;
        this.emocionFiltrada = emocionFiltrada;
        this.periodoConsultado = periodoConsultado;
        this.totalZonasCoincidentes = totalZonasCoincidentes;
        this.zonasFiltradas = zonasFiltradas;
        this.tiempoCalculoMs = tiempoCalculoMs;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getZonaFiltrada() {
        return zonaFiltrada;
    }

    public void setZonaFiltrada(String zonaFiltrada) {
        this.zonaFiltrada = zonaFiltrada;
    }

    public String getEmocionFiltrada() {
        return emocionFiltrada;
    }

    public void setEmocionFiltrada(String emocionFiltrada) {
        this.emocionFiltrada = emocionFiltrada;
    }

    public String getPeriodoConsultado() {
        return periodoConsultado;
    }

    public void setPeriodoConsultado(String periodoConsultado) {
        this.periodoConsultado = periodoConsultado;
    }

    public int getTotalZonasCoincidentes() {
        return totalZonasCoincidentes;
    }

    public void setTotalZonasCoincidentes(int totalZonasCoincidentes) {
        this.totalZonasCoincidentes = totalZonasCoincidentes;
    }

    public List<ZonaTerritorialAnaliticaDto> getZonasFiltradas() {
        return zonasFiltradas;
    }

    public void setZonasFiltradas(List<ZonaTerritorialAnaliticaDto> zonasFiltradas) {
        this.zonasFiltradas = zonasFiltradas;
    }

    public long getTiempoCalculoMs() {
        return tiempoCalculoMs;
    }

    public void setTiempoCalculoMs(long tiempoCalculoMs) {
        this.tiempoCalculoMs = tiempoCalculoMs;
    }

    public static class Constructor {
        private String zonaFiltrada;
        private String emocionFiltrada;
        private String periodoConsultado;
        private int totalZonasCoincidentes;
        private List<ZonaTerritorialAnaliticaDto> zonasFiltradas;
        private long tiempoCalculoMs;

        public Constructor zonaFiltrada(String zonaFiltrada) {
            this.zonaFiltrada = zonaFiltrada;
            return this;
        }

        public Constructor emocionFiltrada(String emocionFiltrada) {
            this.emocionFiltrada = emocionFiltrada;
            return this;
        }

        public Constructor periodoConsultado(String periodoConsultado) {
            this.periodoConsultado = periodoConsultado;
            return this;
        }

        public Constructor totalZonasCoincidentes(int totalZonasCoincidentes) {
            this.totalZonasCoincidentes = totalZonasCoincidentes;
            return this;
        }

        public Constructor zonasFiltradas(List<ZonaTerritorialAnaliticaDto> zonasFiltradas) {
            this.zonasFiltradas = zonasFiltradas;
            return this;
        }

        public Constructor tiempoCalculoMs(long tiempoCalculoMs) {
            this.tiempoCalculoMs = tiempoCalculoMs;
            return this;
        }

        public FiltroTerritorialDashboardRespuesta build() {
            return new FiltroTerritorialDashboardRespuesta(
                    zonaFiltrada, emocionFiltrada, periodoConsultado,
                    totalZonasCoincidentes, zonasFiltradas, tiempoCalculoMs
            );
        }
    }
}
