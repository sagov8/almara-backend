package com.almara.modules.institutional.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de registro de cliente institucional (HU-16).
 */
public class RegistroInstitucionalSolicitud {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede exceder 150 caracteres")
    private String razonSocial;

    @NotBlank(message = "La identificación fiscal o NIT es obligatoria")
    @Size(max = 30, message = "El NIT no puede exceder 30 caracteres")
    private String identificacionFiscalNit;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Size(max = 80, message = "El nombre de contacto no puede exceder 80 caracteres")
    private String nombreContacto;

    @NotBlank(message = "El correo corporativo es obligatorio")
    @Email(message = "El formato de correo no es válido")
    @Size(max = 100, message = "El correo corporativo no puede exceder 100 caracteres")
    private String correoCorporativo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 255, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefonoContacto;

    private String sectorActividad;

    public RegistroInstitucionalSolicitud() {
    }

    public RegistroInstitucionalSolicitud(String razonSocial, String identificacionFiscalNit, String nombreContacto,
                                         String correoCorporativo, String password, String telefonoContacto,
                                         String sectorActividad) {
        this.razonSocial = razonSocial;
        this.identificacionFiscalNit = identificacionFiscalNit;
        this.nombreContacto = nombreContacto;
        this.correoCorporativo = correoCorporativo;
        this.password = password;
        this.telefonoContacto = telefonoContacto;
        this.sectorActividad = sectorActividad;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getIdentificacionFiscalNit() {
        return identificacionFiscalNit;
    }

    public void setIdentificacionFiscalNit(String identificacionFiscalNit) {
        this.identificacionFiscalNit = identificacionFiscalNit;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getCorreoCorporativo() {
        return correoCorporativo;
    }

    public void setCorreoCorporativo(String correoCorporativo) {
        this.correoCorporativo = correoCorporativo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getSectorActividad() {
        return sectorActividad;
    }

    public void setSectorActividad(String sectorActividad) {
        this.sectorActividad = sectorActividad;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private String razonSocial;
        private String identificacionFiscalNit;
        private String nombreContacto;
        private String correoCorporativo;
        private String password;
        private String telefonoContacto;
        private String sectorActividad;

        public Constructor razonSocial(String razonSocial) {
            this.razonSocial = razonSocial;
            return this;
        }

        public Constructor identificacionFiscalNit(String identificacionFiscalNit) {
            this.identificacionFiscalNit = identificacionFiscalNit;
            return this;
        }

        public Constructor nombreContacto(String nombreContacto) {
            this.nombreContacto = nombreContacto;
            return this;
        }

        public Constructor correoCorporativo(String correoCorporativo) {
            this.correoCorporativo = correoCorporativo;
            return this;
        }

        public Constructor password(String password) {
            this.password = password;
            return this;
        }

        public Constructor telefonoContacto(String telefonoContacto) {
            this.telefonoContacto = telefonoContacto;
            return this;
        }

        public Constructor sectorActividad(String sectorActividad) {
            this.sectorActividad = sectorActividad;
            return this;
        }

        public RegistroInstitucionalSolicitud build() {
            return new RegistroInstitucionalSolicitud(razonSocial, identificacionFiscalNit, nombreContacto,
                    correoCorporativo, password, telefonoContacto, sectorActividad);
        }
    }
}
