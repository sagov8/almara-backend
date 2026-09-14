package com.almara.modules.emotion.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para la entrada de selección y registro de emoción ciudadana (HU-01 y HU-04).
 * Admite coordenadas GPS temporales (latitud, longitud) o un identificador de zona manual.
 * Garantiza Privacy by Design: los campos de ubicación exacta no se persisten ni se registran en bitácoras.
 */
public class RegistroEmocionSolicitud {

    @NotNull(message = "La emoción seleccionada es obligatoria y debe pertenecer al catálogo predeterminado")
    private TipoEmocion emocion;

    @NotBlank(message = "El identificador de sesión anónimo (token_sesion_temporal) es obligatorio")
    @Size(min = 10, max = 64, message = "El token de sesión temporal debe tener entre 10 y 64 caracteres")
    private String tokenSesionTemporal;

    @DecimalMin(value = "-90.0", message = "La latitud mínima válida es -90.0")
    @DecimalMax(value = "90.0", message = "La latitud máxima válida es 90.0")
    private Double latitud;

    @DecimalMin(value = "-180.0", message = "La longitud mínima válida es -180.0")
    @DecimalMax(value = "180.0", message = "La longitud máxima válida es 180.0")
    private Double longitud;

    private String zonaManualId;

    public RegistroEmocionSolicitud() {
    }

    public RegistroEmocionSolicitud(TipoEmocion emocion, String tokenSesionTemporal) {
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
    }

    public RegistroEmocionSolicitud(TipoEmocion emocion, String tokenSesionTemporal, Double latitud, Double longitud, String zonaManualId) {
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
        this.latitud = latitud;
        this.longitud = longitud;
        this.zonaManualId = zonaManualId;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public TipoEmocion getEmocion() {
        return emocion;
    }

    public void setEmocion(TipoEmocion emocion) {
        this.emocion = emocion;
    }

    public String getTokenSesionTemporal() {
        return tokenSesionTemporal;
    }

    public void setTokenSesionTemporal(String tokenSesionTemporal) {
        this.tokenSesionTemporal = tokenSesionTemporal;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getZonaManualId() {
        return zonaManualId;
    }

    public void setZonaManualId(String zonaManualId) {
        this.zonaManualId = zonaManualId;
    }

    public boolean tieneCoordenadasGps() {
        return latitud != null && longitud != null;
    }

    public boolean tieneZonaManual() {
        return zonaManualId != null && !zonaManualId.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "RegistroEmocionSolicitud{" +
                "emocion=" + emocion +
                ", tokenSesionTemporal='" + tokenSesionTemporal + '\'' +
                ", ubicacion=[PROTEGIDA_POR_PRIVACIDAD]" +
                ", zonaManualId='" + zonaManualId + '\'' +
                '}';
    }

    public static class Constructor {
        private TipoEmocion emocion;
        private String tokenSesionTemporal;
        private Double latitud;
        private Double longitud;
        private String zonaManualId;

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor tokenSesionTemporal(String tokenSesionTemporal) {
            this.tokenSesionTemporal = tokenSesionTemporal;
            return this;
        }

        public Constructor latitud(Double latitud) {
            this.latitud = latitud;
            return this;
        }

        public Constructor longitud(Double longitud) {
            this.longitud = longitud;
            return this;
        }

        public Constructor zonaManualId(String zonaManualId) {
            this.zonaManualId = zonaManualId;
            return this;
        }

        public RegistroEmocionSolicitud build() {
            return new RegistroEmocionSolicitud(emocion, tokenSesionTemporal, latitud, longitud, zonaManualId);
        }
    }
}
