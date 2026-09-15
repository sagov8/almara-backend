package com.almara.modules.geo.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA mapeada a la tabla zona del modelo de datos de Almara (almara_dbdiagram.dbml).
 * Representa los sectores urbanos y sus celdas H3 (HU-04, HU-13, HU-14).
 */
@Entity
@Table(name = "zona")
public class EntidadZona {

    @Id
    @Column(name = "id_celda_h3", length = 15, nullable = false)
    private String idCeldaH3;

    @Column(name = "resolucion_h3", nullable = false)
    private Integer resolucionH3;

    @Column(name = "latitud_centroide", nullable = false)
    private Double latitudCentroide;

    @Column(name = "longitud_centroide", nullable = false)
    private Double longitudCentroide;

    @Column(name = "zona_manual_id", length = 50, unique = true)
    private String zonaManualId;

    @Column(name = "nombre_zona_manual", length = 100)
    private String nombreZonaManual;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "umbral_minimo", nullable = false)
    private Integer umbralMinimo;

    public EntidadZona() {
    }

    public EntidadZona(String idCeldaH3, Integer resolucionH3, Double latitudCentroide,
                       Double longitudCentroide, String zonaManualId, String nombreZonaManual,
                       String descripcion, Integer umbralMinimo) {
        this.idCeldaH3 = idCeldaH3;
        this.resolucionH3 = resolucionH3;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
        this.zonaManualId = zonaManualId;
        this.nombreZonaManual = nombreZonaManual;
        this.descripcion = descripcion;
        this.umbralMinimo = umbralMinimo;
    }

    public EntidadZona(String idCeldaH3, Integer resolucionH3, Double latitudCentroide,
                       Double longitudCentroide, String zonaManualId, String nombreZonaManual,
                       Integer umbralMinimo) {
        this(idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide, zonaManualId, nombreZonaManual, null, umbralMinimo);
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public Integer getResolucionH3() {
        return resolucionH3;
    }

    public void setResolucionH3(Integer resolucionH3) {
        this.resolucionH3 = resolucionH3;
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

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public String getNombreZonaManual() {
        return nombreZonaManual;
    }

    public void setNombreZonaManual(String nombreZonaManual) {
        this.nombreZonaManual = nombreZonaManual;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getUmbralMinimo() {
        return umbralMinimo;
    }

    public void setUmbralMinimo(Integer umbralMinimo) {
        this.umbralMinimo = umbralMinimo;
    }

    public static class Constructor {
        private String idCeldaH3;
        private Integer resolucionH3 = 9;
        private Double latitudCentroide;
        private Double longitudCentroide;
        private String zonaManualId;
        private String nombreZonaManual;
        private String descripcion;
        private Integer umbralMinimo = 5;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor resolucionH3(Integer resolucionH3) {
            this.resolucionH3 = resolucionH3;
            return this;
        }

        public Constructor latitudCentroide(Double latitudCentroide) {
            this.latitudCentroide = latitudCentroide;
            return this;
        }

        public Constructor longitudCentroide(Double longitudCentroide) {
            this.longitudCentroide = longitudCentroide;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor nombreZonaManual(String nombreZonaManual) {
            this.nombreZonaManual = nombreZonaManual;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Constructor umbralMinimo(Integer umbralMinimo) {
            this.umbralMinimo = umbralMinimo;
            return this;
        }

        public EntidadZona build() {
            return new EntidadZona(idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide, zonaManualId, nombreZonaManual, descripcion, umbralMinimo);
        }
    }
}
