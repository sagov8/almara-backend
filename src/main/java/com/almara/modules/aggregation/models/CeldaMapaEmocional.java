package com.almara.modules.aggregation.models;

import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.geo.models.CoordenadasGps;

import java.util.List;

/**
 * DTO que representa una celda hexagonal visualizable en el mapa interactivo (HU-05).
 * Cumple con RNF Seguridad y Privacidad Colectiva: no expone identificadores de usuario,
 * solo métricas agregadas que cumplan con el umbral mínimo de participación ciudadana.
 */
public class CeldaMapaEmocional {

    private String idCeldaH3;
    private int resolucionH3;
    private double latitudCentroide;
    private double longitudCentroide;
    private List<CoordenadasGps> verticesHexagono;
    private TipoEmocion emocionPredominante;
    private String nombreEmocion;
    private String codigoHexColor;
    private String codigoHexFondo;
    private String icono;
    private int totalReportes;
    private boolean cumpleUmbral;
    private String nombreZona;
    private String descripcionProteccion;

    public CeldaMapaEmocional() {
    }

    public CeldaMapaEmocional(String idCeldaH3, int resolucionH3, double latitudCentroide, double longitudCentroide,
                              List<CoordenadasGps> verticesHexagono, TipoEmocion emocionPredominante,
                              String nombreEmocion, String codigoHexColor, String codigoHexFondo, String icono,
                              int totalReportes, boolean cumpleUmbral, String nombreZona, String descripcionProteccion) {
        this.idCeldaH3 = idCeldaH3;
        this.resolucionH3 = resolucionH3;
        this.latitudCentroide = latitudCentroide;
        this.longitudCentroide = longitudCentroide;
        this.verticesHexagono = verticesHexagono;
        this.emocionPredominante = emocionPredominante;
        this.nombreEmocion = nombreEmocion;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.icono = icono;
        this.totalReportes = totalReportes;
        this.cumpleUmbral = cumpleUmbral;
        this.nombreZona = nombreZona;
        this.descripcionProteccion = descripcionProteccion;
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

    public int getResolucionH3() {
        return resolucionH3;
    }

    public void setResolucionH3(int resolucionH3) {
        this.resolucionH3 = resolucionH3;
    }

    public double getLatitudCentroide() {
        return latitudCentroide;
    }

    public void setLatitudCentroide(double latitudCentroide) {
        this.latitudCentroide = latitudCentroide;
    }

    public double getLongitudCentroide() {
        return longitudCentroide;
    }

    public void setLongitudCentroide(double longitudCentroide) {
        this.longitudCentroide = longitudCentroide;
    }

    public List<CoordenadasGps> getVerticesHexagono() {
        return verticesHexagono;
    }

    public void setVerticesHexagono(List<CoordenadasGps> verticesHexagono) {
        this.verticesHexagono = verticesHexagono;
    }

    public TipoEmocion getEmocionPredominante() {
        return emocionPredominante;
    }

    public void setEmocionPredominante(TipoEmocion emocionPredominante) {
        this.emocionPredominante = emocionPredominante;
    }

    public String getNombreEmocion() {
        return nombreEmocion;
    }

    public void setNombreEmocion(String nombreEmocion) {
        this.nombreEmocion = nombreEmocion;
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

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public int getTotalReportes() {
        return totalReportes;
    }

    public void setTotalReportes(int totalReportes) {
        this.totalReportes = totalReportes;
    }

    public boolean isCumpleUmbral() {
        return cumpleUmbral;
    }

    public void setCumpleUmbral(boolean cumpleUmbral) {
        this.cumpleUmbral = cumpleUmbral;
    }

    public String getNombreZona() {
        return nombreZona;
    }

    public void setNombreZona(String nombreZona) {
        this.nombreZona = nombreZona;
    }

    public String getDescripcionProteccion() {
        return descripcionProteccion;
    }

    public void setDescripcionProteccion(String descripcionProteccion) {
        this.descripcionProteccion = descripcionProteccion;
    }

    public static class Constructor {
        private String idCeldaH3;
        private int resolucionH3;
        private double latitudCentroide;
        private double longitudCentroide;
        private List<CoordenadasGps> verticesHexagono;
        private TipoEmocion emocionPredominante;
        private String nombreEmocion;
        private String codigoHexColor;
        private String codigoHexFondo;
        private String icono;
        private int totalReportes;
        private boolean cumpleUmbral;
        private String nombreZona;
        private String descripcionProteccion;

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor resolucionH3(int resolucionH3) {
            this.resolucionH3 = resolucionH3;
            return this;
        }

        public Constructor latitudCentroide(double latitudCentroide) {
            this.latitudCentroide = latitudCentroide;
            return this;
        }

        public Constructor longitudCentroide(double longitudCentroide) {
            this.longitudCentroide = longitudCentroide;
            return this;
        }

        public Constructor verticesHexagono(List<CoordenadasGps> verticesHexagono) {
            this.verticesHexagono = verticesHexagono;
            return this;
        }

        public Constructor emocionPredominante(TipoEmocion emocionPredominante) {
            this.emocionPredominante = emocionPredominante;
            return this;
        }

        public Constructor nombreEmocion(String nombreEmocion) {
            this.nombreEmocion = nombreEmocion;
            return this;
        }

        public Constructor codigoHexColor(String codigoHexColor) {
            this.codigoHexColor = codigoHexColor;
            return this;
        }

        public Constructor codigoHexFondo(String codigoHexFondo) {
            this.codigoHexFondo = codigoHexFondo;
            return this;
        }

        public Constructor icono(String icono) {
            this.icono = icono;
            return this;
        }

        public Constructor totalReportes(int totalReportes) {
            this.totalReportes = totalReportes;
            return this;
        }

        public Constructor cumpleUmbral(boolean cumpleUmbral) {
            this.cumpleUmbral = cumpleUmbral;
            return this;
        }

        public Constructor nombreZona(String nombreZona) {
            this.nombreZona = nombreZona;
            return this;
        }

        public Constructor descripcionProteccion(String descripcionProteccion) {
            this.descripcionProteccion = descripcionProteccion;
            return this;
        }

        public CeldaMapaEmocional build() {
            return new CeldaMapaEmocional(idCeldaH3, resolucionH3, latitudCentroide, longitudCentroide,
                    verticesHexagono, emocionPredominante, nombreEmocion, codigoHexColor, codigoHexFondo,
                    icono, totalReportes, cumpleUmbral, nombreZona, descripcionProteccion);
        }
    }
}
