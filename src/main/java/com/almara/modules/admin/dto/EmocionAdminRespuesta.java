package com.almara.modules.admin.dto;

import java.time.Instant;

/**
 * Proyección de una emoción en el panel administrativo con estado y métricas históricas asociadas (HU-12).
 */
public class EmocionAdminRespuesta {

    private String idEmocion;
    private String nombreEtiqueta;
    private String codigoHexColor;
    private String codigoHexFondo;
    private String iconoSvg;
    private String descripcion;
    private boolean activo;
    private long totalReportesAsociados;
    private Instant fechaCreacion;

    public EmocionAdminRespuesta() {
    }

    public EmocionAdminRespuesta(String idEmocion, String nombreEtiqueta, String codigoHexColor,
                                 String codigoHexFondo, String iconoSvg, String descripcion,
                                 boolean activo, long totalReportesAsociados, Instant fechaCreacion) {
        this.idEmocion = idEmocion;
        this.nombreEtiqueta = nombreEtiqueta;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.iconoSvg = iconoSvg;
        this.descripcion = descripcion;
        this.activo = activo;
        this.totalReportesAsociados = totalReportesAsociados;
        this.fechaCreacion = fechaCreacion;
    }

    public String getIdEmocion() {
        return idEmocion;
    }

    public void setIdEmocion(String idEmocion) {
        this.idEmocion = idEmocion;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = codigoHexColor;
    }

    public String getCodigoHexFondo() {
        return codigoHexFondo;
    }

    public void setCodigoHexFondo(String codigoHexFondo) {
        this.codigoHexFondo = codigoHexFondo;
    }

    public String getIconoSvg() {
        return iconoSvg;
    }

    public void setIconoSvg(String iconoSvg) {
        this.iconoSvg = iconoSvg;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public long getTotalReportesAsociados() {
        return totalReportesAsociados;
    }

    public void setTotalReportesAsociados(long totalReportesAsociados) {
        this.totalReportesAsociados = totalReportesAsociados;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
