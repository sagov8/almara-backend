package com.almara.common;

import java.time.Instant;
import java.util.Map;

/**
 * Estructura estandarizada para responder errores en la API de Almara.
 */
public class RespuestaError {

    private Instant marcaTemporal;
    private int codigoEstado;
    private String error;
    private String mensaje;
    private String ruta;
    private Map<String, Object> detallesAdicionales;

    public RespuestaError() {
    }

    public RespuestaError(Instant marcaTemporal, int codigoEstado, String error, String mensaje, String ruta, Map<String, Object> detallesAdicionales) {
        this.marcaTemporal = marcaTemporal;
        this.codigoEstado = codigoEstado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.detallesAdicionales = detallesAdicionales;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public Instant getMarcaTemporal() {
        return marcaTemporal;
    }

    public void setMarcaTemporal(Instant marcaTemporal) {
        this.marcaTemporal = marcaTemporal;
    }

    public int getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(int codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public Map<String, Object> getDetallesAdicionales() {
        return detallesAdicionales;
    }

    public void setDetallesAdicionales(Map<String, Object> detallesAdicionales) {
        this.detallesAdicionales = detallesAdicionales;
    }

    public static class Constructor {
        private Instant marcaTemporal;
        private int codigoEstado;
        private String error;
        private String mensaje;
        private String ruta;
        private Map<String, Object> detallesAdicionales;

        public Constructor marcaTemporal(Instant marcaTemporal) {
            this.marcaTemporal = marcaTemporal;
            return this;
        }

        public Constructor codigoEstado(int codigoEstado) {
            this.codigoEstado = codigoEstado;
            return this;
        }

        public Constructor error(String error) {
            this.error = error;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public Constructor ruta(String ruta) {
            this.ruta = ruta;
            return this;
        }

        public Constructor detallesAdicionales(Map<String, Object> detallesAdicionales) {
            this.detallesAdicionales = detallesAdicionales;
            return this;
        }

        public RespuestaError build() {
            return new RespuestaError(marcaTemporal, codigoEstado, error, mensaje, ruta, detallesAdicionales);
        }
    }
}
