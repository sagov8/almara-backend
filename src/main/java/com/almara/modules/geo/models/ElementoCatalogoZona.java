package com.almara.modules.geo.models;

/**
 * DTO para exponer las zonas del catálogo predefinido para selección manual
 * cuando el usuario decide no otorgar permisos GPS
 */
public class ElementoCatalogoZona {

    private String zonaManualId;
    private String nombre;
    private String descripcion;
    private String idCeldaH3;
    private int resolucionH3;
    private Double latitudCentroide;
    private Double longitudCentroide;

    public ElementoCatalogoZona() {
    }

    public ElementoCatalogoZona(String zonaManualId, String nombre, String descripcion, String idCeldaH3,
                                int resolucionH3, Double latitudCentroide, Double longitudCentroide) {
        this.zonaManualId = zonaManualId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idCeldaH3 = idCeldaH3;
        this.resolucionH3 = resolucionH3;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
    }

    public ElementoCatalogoZona(String zonaManualId, String nombre, String idCeldaH3,
                                int resolucionH3, Double latitudCentroide, Double longitudCentroide) {
        this(zonaManualId, nombre, null, idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide);
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public int getResolucionH3() {
        return resolucionH3;
    }

    public void setResolucionH3(int resolucionH3) {
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

    public static class Constructor {
        private String zonaManualId;
        private String nombre;
        private String descripcion;
        private String idCeldaH3;
        private int resolucionH3;
        private Double latitudCentroide;
        private Double longitudCentroide;

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public Constructor nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor resolucionH3(int resolucionH3) {
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

        public ElementoCatalogoZona build() {
            return new ElementoCatalogoZona(zonaManualId, nombre, descripcion, idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide);
        }
    }
}
