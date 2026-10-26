package com.almara.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud administrativa para modificar los atributos visuales y descriptivos de una emoción (HU-12: include Modificar).
 * Nota: El idEmocion es inmutable para garantizar la integridad referencial con los registros históricos.
 */
public class ModificarEmocionSolicitud {

    @NotBlank(message = "El nombre o etiqueta visible es obligatorio")
    @Size(max = 30, message = "El nombre de la etiqueta no puede exceder 30 caracteres")
    private String nombreEtiqueta;

    @NotBlank(message = "El código hexadecimal del color es obligatorio")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El código de color debe cumplir con la estructura hexadecimal de 7 caracteres (#RRGGBB)")
    private String codigoHexColor;

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "El código de fondo debe cumplir con la estructura hexadecimal de 7 caracteres (#RRGGBB)")
    private String codigoHexFondo;

    @NotBlank(message = "El ícono o recurso visual es obligatorio")
    @Size(max = 255, message = "El ícono no puede exceder 255 caracteres")
    private String iconoSvg;

    @Size(max = 150, message = "La descripción contextual no puede exceder 150 caracteres")
    private String descripcion;

    public ModificarEmocionSolicitud() {
    }

    public ModificarEmocionSolicitud(String nombreEtiqueta, String codigoHexColor,
                                     String codigoHexFondo, String iconoSvg, String descripcion) {
        this.nombreEtiqueta = nombreEtiqueta;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.iconoSvg = iconoSvg;
        this.descripcion = descripcion;
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta;
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

    public String getIconoSvg() {
        return iconoSvg;
    }

    public void setIconoSvg(String iconoSvg) {
        this.iconoSvg = iconoSvg;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
