package com.almara.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;

/**
 * Solicitud administrativa para dar de alta una nueva emoción en el catálogo (HU-12: include Agregar).
 */
public class CrearEmocionSolicitud {

    @NotBlank(message = "El identificador de la emoción es obligatorio")
    @Size(min = 2, max = 20, message = "El identificador debe tener entre 2 y 20 caracteres")
    @Pattern(regexp = "^[A-Z0-9_]{2,20}$", message = "El identificador debe ser alfanumérico en mayúsculas sin espacios (ej. FELICIDAD, CALMA, SORPRESA)")
    private String idEmocion;

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

    public CrearEmocionSolicitud() {
    }

    public CrearEmocionSolicitud(String idEmocion, String nombreEtiqueta, String codigoHexColor,
                                 String codigoHexFondo, String iconoSvg, String descripcion) {
        setIdEmocion(idEmocion);
        setNombreEtiqueta(nombreEtiqueta);
        setCodigoHexColor(codigoHexColor);
        setCodigoHexFondo(codigoHexFondo);
        setIconoSvg(iconoSvg);
        setDescripcion(descripcion);
    }

    /**
     * Normaliza el identificador quitando tildes/diacríticos, convirtiendo espacios o guiones en '_'
     * y pasándolo a mayúsculas para evitar rechazos por acentos en español (ej. "Alegría" -> "ALEGRIA").
     */
    private static String normalizarId(String valor) {
        if (valor == null) {
            return null;
        }
        String sinTildes = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('ñ', 'n')
                .replace('Ñ', 'N');
        return sinTildes
                .toUpperCase()
                .replaceAll("[\\s\\-]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static String normalizarHex(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim().toUpperCase();
        if (limpio.matches("^[0-9A-F]{6}$")) {
            return "#" + limpio;
        }
        return limpio;
    }

    public String getIdEmocion() {
        return idEmocion;
    }

    public void setIdEmocion(String idEmocion) {
        this.idEmocion = normalizarId(idEmocion);
    }

    public String getNombreEtiqueta() {
        return nombreEtiqueta;
    }

    public void setNombreEtiqueta(String nombreEtiqueta) {
        this.nombreEtiqueta = nombreEtiqueta != null ? nombreEtiqueta.trim() : null;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = normalizarHex(codigoHexColor);
    }

    public String getCodigoHexFondo() {
        return codigoHexFondo;
    }

    public void setCodigoHexFondo(String codigoHexFondo) {
        this.codigoHexFondo = normalizarHex(codigoHexFondo);
    }

    public String getIconoSvg() {
        return iconoSvg;
    }

    public void setIconoSvg(String iconoSvg) {
        this.iconoSvg = iconoSvg != null ? iconoSvg.trim() : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : null;
    }
}
