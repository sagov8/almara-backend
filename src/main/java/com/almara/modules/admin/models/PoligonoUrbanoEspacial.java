package com.almara.modules.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un polígono urbano espacial delimitado para la ciudad (HU-14).
 * Almacena la geometría GeoJSON cerrada y su indexación espacial con el sistema Uber H3 / PostGIS.
 */
@Entity
@Table(name = "poligono_urbano_espacial")
public class PoligonoUrbanoEspacial {

    @Id
    @Column(name = "id_zona", length = 50, nullable = false)
    private String idZona;

    @Column(name = "nombre_zona", length = 100, nullable = false)
    private String nombreZona;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "geometria_geojson", columnDefinition = "TEXT", nullable = false)
    private String geometriaGeoJson;

    @Column(name = "latitud_centroide", nullable = false)
    private Double latitudCentroide;

    @Column(name = "longitud_centroide", nullable = false)
    private Double longitudCentroide;

    @Column(name = "resolucion_h3", nullable = false)
    private int resolucionH3;

    @Column(name = "id_celda_h3_centroide", length = 20, nullable = false)
    private String idCeldaH3Centroide;

    @Column(name = "total_celdas_h3_cubiertas", nullable = false)
    private int totalCeldasH3Cubiertas;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por", length = 100, nullable = false)
    private String actualizadoPor;

    public PoligonoUrbanoEspacial() {
    }

    public PoligonoUrbanoEspacial(String idZona, String nombreZona, String descripcion,
                                 String geometriaGeoJson, Double latitudCentroide,
                                 Double longitudCentroide, int resolucionH3,
                                 String idCeldaH3Centroide, int totalCeldasH3Cubiertas,
                                 boolean activo, String actualizadoPor) {
        this.idZona = idZona;
        this.nombreZona = nombreZona;
        this.descripcion = descripcion;
        this.geometriaGeoJson = geometriaGeoJson;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
        this.resolucionH3 = resolucionH3;
        this.idCeldaH3Centroide = idCeldaH3Centroide;
        this.totalCeldasH3Cubiertas = totalCeldasH3Cubiertas;
        this.activo = activo;
        this.actualizadoPor = actualizadoPor;
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (fechaActualizacion == null) {
            fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public String getIdZona() {
        return idZona;
    }

    public void setIdZona(String idZona) {
        this.idZona = idZona;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGeometriaGeoJson() {
        return geometriaGeoJson;
    }

    public void setGeometriaGeoJson(String geometriaGeoJson) {
        this.geometriaGeoJson = geometriaGeoJson;
    }

    public Double getLatitudCentroide() {
        return latitudCentroide;
    }

    public void setLatitudCentroide(Double latitudCentroide) {
        this.latitudCentroide = latitudCentroide;
    }

    public Double getLongitudCentroide() {
        return longitudCentroide;
    }

    public void setLongitudCentroide(Double longitudCentroide) {
        this.longitudCentroide = longitudCentroide;
    }

    public int getResolucionH3() {
        return resolucionH3;
    }

    public void setResolucionH3(int resolucionH3) {
        this.resolucionH3 = resolucionH3;
    }

    public String getIdCeldaH3Centroide() {
        return idCeldaH3Centroide;
    }

    public void setIdCeldaH3Centroide(String idCeldaH3Centroide) {
        this.idCeldaH3Centroide = idCeldaH3Centroide;
    }

    public int getTotalCeldasH3Cubiertas() {
        return totalCeldasH3Cubiertas;
    }

    public void setTotalCeldasH3Cubiertas(int totalCeldasH3Cubiertas) {
        this.totalCeldasH3Cubiertas = totalCeldasH3Cubiertas;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }
}
