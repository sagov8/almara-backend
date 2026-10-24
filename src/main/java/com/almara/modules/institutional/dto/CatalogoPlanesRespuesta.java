package com.almara.modules.institutional.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que encapsula la respuesta del Catálogo de Planes SaaS (HU-19).
 */
public class CatalogoPlanesRespuesta {

    private List<PlanSuscripcionCatalogoDto> planes = new ArrayList<>();
    private int totalPlanesActivos;
    private String codigoPlanActualCliente;
    private long tiempoConsultaMs;

    public CatalogoPlanesRespuesta() {
    }

    public CatalogoPlanesRespuesta(List<PlanSuscripcionCatalogoDto> planes, int totalPlanesActivos,
                                   String codigoPlanActualCliente, long tiempoConsultaMs) {
        this.planes = planes != null ? planes : new ArrayList<>();
        this.totalPlanesActivos = totalPlanesActivos;
        this.codigoPlanActualCliente = codigoPlanActualCliente;
        this.tiempoConsultaMs = tiempoConsultaMs;
    }

    public List<PlanSuscripcionCatalogoDto> getPlanes() {
        return planes;
    }

    public void setPlanes(List<PlanSuscripcionCatalogoDto> planes) {
        this.planes = planes;
    }

    public int getTotalPlanesActivos() {
        return totalPlanesActivos;
    }

    public void setTotalPlanesActivos(int totalPlanesActivos) {
        this.totalPlanesActivos = totalPlanesActivos;
    }

    public String getCodigoPlanActualCliente() {
        return codigoPlanActualCliente;
    }

    public void setCodigoPlanActualCliente(String codigoPlanActualCliente) {
        this.codigoPlanActualCliente = codigoPlanActualCliente;
    }

    public long getTiempoConsultaMs() {
        return tiempoConsultaMs;
    }

    public void setTiempoConsultaMs(long tiempoConsultaMs) {
        this.tiempoConsultaMs = tiempoConsultaMs;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private List<PlanSuscripcionCatalogoDto> planes = new ArrayList<>();
        private int totalPlanesActivos;
        private String codigoPlanActualCliente;
        private long tiempoConsultaMs;

        public Constructor planes(List<PlanSuscripcionCatalogoDto> planes) {
            this.planes = planes;
            return this;
        }

        public Constructor totalPlanesActivos(int totalPlanesActivos) {
            this.totalPlanesActivos = totalPlanesActivos;
            return this;
        }

        public Constructor codigoPlanActualCliente(String codigoPlanActualCliente) {
            this.codigoPlanActualCliente = codigoPlanActualCliente;
            return this;
        }

        public Constructor tiempoConsultaMs(long tiempoConsultaMs) {
            this.tiempoConsultaMs = tiempoConsultaMs;
            return this;
        }

        public CatalogoPlanesRespuesta build() {
            return new CatalogoPlanesRespuesta(planes, totalPlanesActivos, codigoPlanActualCliente, tiempoConsultaMs);
        }
    }
}
