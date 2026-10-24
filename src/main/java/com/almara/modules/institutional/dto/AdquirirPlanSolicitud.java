package com.almara.modules.institutional.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de adquisición de plan de suscripción SaaS para checkout (HU-22).
 */
public class AdquirirPlanSolicitud {

    @NotBlank(message = "El código del plan es obligatorio")
    private String codigoPlan;

    private String metodoPagoPreferido;

    public AdquirirPlanSolicitud() {
    }

    public AdquirirPlanSolicitud(String codigoPlan, String metodoPagoPreferido) {
        this.codigoPlan = codigoPlan;
        this.metodoPagoPreferido = metodoPagoPreferido;
    }

    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(String codigoPlan) {
        this.codigoPlan = codigoPlan;
    }

    public String getMetodoPagoPreferido() {
        return metodoPagoPreferido;
    }

    public void setMetodoPagoPreferido(String metodoPagoPreferido) {
        this.metodoPagoPreferido = metodoPagoPreferido;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String codigoPlan;
        private String metodoPagoPreferido;

        public Constructor codigoPlan(String codigoPlan) {
            this.codigoPlan = codigoPlan;
            return this;
        }

        public Constructor metodoPagoPreferido(String metodoPagoPreferido) {
            this.metodoPagoPreferido = metodoPagoPreferido;
            return this;
        }

        public AdquirirPlanSolicitud build() {
            return new AdquirirPlanSolicitud(codigoPlan, metodoPagoPreferido);
        }
    }
}
