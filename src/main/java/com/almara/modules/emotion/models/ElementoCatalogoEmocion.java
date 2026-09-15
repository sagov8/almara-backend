package com.almara.modules.emotion.models;

/**
 * DTO para exponer las propiedades completas de una emoción en el catálogo público.
 */
public class ElementoCatalogoEmocion {

    private TipoEmocion id;
    private String etiquetaVisible;
    private String codigoHexColor;
    private String codigoHexFondo;
    private String iconoSvg;
    private String descripcion;

    public ElementoCatalogoEmocion() {
    }

    public ElementoCatalogoEmocion(TipoEmocion id, String etiquetaVisible, String codigoHexColor, String codigoHexFondo, String iconoSvg, String descripcion) {
        this.id = id;
        this.etiquetaVisible = etiquetaVisible;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
        this.iconoSvg = iconoSvg;
        this.descripcion = descripcion;
    }

    public ElementoCatalogoEmocion(TipoEmocion id, String etiquetaVisible, String codigoHexColor, String codigoHexFondo) {
        this(id, etiquetaVisible, codigoHexColor, codigoHexFondo, null, null);
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public TipoEmocion getId() {
        return id;
    }

    public void setId(TipoEmocion id) {
        this.id = id;
    }

    public String getEtiquetaVisible() {
        return etiquetaVisible;
    }

    public void setEtiquetaVisible(String etiquetaVisible) {
        this.etiquetaVisible = etiquetaVisible;
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

    public static class Constructor {
        private TipoEmocion id;
        private String etiquetaVisible;
        private String codigoHexColor;
        private String codigoHexFondo;
        private String iconoSvg;
        private String descripcion;

        public Constructor id(TipoEmocion id) {
            this.id = id;
            return this;
        }

        public Constructor etiquetaVisible(String etiquetaVisible) {
            this.etiquetaVisible = etiquetaVisible;
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

        public Constructor iconoSvg(String iconoSvg) {
            this.iconoSvg = iconoSvg;
            return this;
        }

        public Constructor descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public ElementoCatalogoEmocion build() {
            return new ElementoCatalogoEmocion(id, etiquetaVisible, codigoHexColor, codigoHexFondo, iconoSvg, descripcion);
        }
    }
}
