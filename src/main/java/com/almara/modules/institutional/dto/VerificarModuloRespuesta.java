package com.almara.modules.institutional.dto;

/**
 * DTO para la respuesta de verificación de permiso de acceso a un módulo (HU-20).
 */
public class VerificarModuloRespuesta {

    private boolean accesoPermitido;
    private String codigoModulo;
    private String codigoPlan;
    private String estadoSuscripcion;
    private String mensaje;

    public VerificarModuloRespuesta() {
    }

    public VerificarModuloRespuesta(boolean accesoPermitido, String codigoModulo, String codigoPlan,
                                   String estadoSuscripcion, String mensaje) {
        this.accesoPermitido = accesoPermitido;
        this.codigoModulo = codigoModulo;
        this.codigoPlan = codigoPlan;
        this.estadoSuscripcion = estadoSuscripcion;
        this.mensaje = mensaje;
    }

    public boolean isAccesoPermitido() {
        return accesoPermitido;
    }

    public void setAccesoPermitido(boolean accesoPermitido) {
        this.accesoPermitido = accesoPermitido;
    }

    public String getCodigoModulo() {
        return codigoModulo;
    }

    public void setCodigoModulo(String codigoModulo) {
        this.codigoModulo = codigoModulo;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getEstadoSuscripcion() {
        return estadoSuscripcion;
    }

    public void setEstadoSuscripcion(String estadoSuscripcion) {
        this.estadoSuscripcion = estadoSuscripcion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private boolean accesoPermitido;
        private String codigoModulo;
        private String codigoPlan;
        private String estadoSuscripcion;
        private String mensaje = "Acceso autorizado al módulo";

        public Constructor accesoPermitido(boolean accesoPermitido) {
            this.accesoPermitido = accesoPermitido;
            return this;
        }

        public Constructor codigoModulo(String codigoModulo) {
            this.codigoModulo = codigoModulo;
            return this;
        }

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor estadoSuscripcion(String estadoSuscripcion) {
            this.estadoSuscripcion = estadoSuscripcion;
            return this;
        }

        public Constructor mensaje(String mensaje) {
            this.mensaje = mensaje;
            return this;
        }

        public VerificarModuloRespuesta build() {
            return new VerificarModuloRespuesta(accesoPermitido, codigoModulo, codigoPlan, estadoSuscripcion, mensaje);
        }
    }
}
