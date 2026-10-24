package com.almara.modules.institutional.dto;

/**
 * DTO que representa un indicador agregado de emoción para la ciudad de Popayán (HU-24).
 */
public class IndicadorEmocionCiudadDto {

    private String idEmocion;
    private String nombreEmocion;
    private String codigoHexColor;
    private String icono;
    private int porcentajeGlobal;

    public IndicadorEmocionCiudadDto() {
    }

    public IndicadorEmocionCiudadDto(String idEmocion, String nombreEmocion, String codigoHexColor,
                                     String icono, int porcentajeGlobal) {
        this.idEmocion = idEmocion;
        this.nombreEmocion = nombreEmocion;
        this.codigoHexColor = codigoHexColor;
        this.icono = icono;
        this.porcentajeGlobal = porcentajeGlobal;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getIdEmocion() {
        return idEmocion;
    }

    public void setIdEmocion(String idEmocion) {
        this.idEmocion = idEmocion;
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

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public int getPorcentajeGlobal() {
        return porcentajeGlobal;
    }

    public void setPorcentajeGlobal(int porcentajeGlobal) {
        this.porcentajeGlobal = porcentajeGlobal;
    }

    public static class Constructor {
        private String idEmocion;
        private String nombreEmocion;
        private String codigoHexColor;
        private String icono;
        private int porcentajeGlobal;

        public Constructor idEmocion(String idEmocion) {
            this.idEmocion = idEmocion;
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

        public Constructor icono(String icono) {
            this.icono = icono;
            return this;
        }

        public Constructor porcentajeGlobal(int porcentajeGlobal) {
            this.porcentajeGlobal = porcentajeGlobal;
            return this;
        }

        public IndicadorEmocionCiudadDto build() {
            return new IndicadorEmocionCiudadDto(idEmocion, nombreEmocion, codigoHexColor, icono, porcentajeGlobal);
        }
    }
}
