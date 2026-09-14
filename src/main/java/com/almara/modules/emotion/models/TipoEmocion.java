package com.almara.modules.emotion.models;

/**
 * Catálogo predeterminado de emociones soportadas por Almara.
 * Coincide con las opciones del mockup y el requisito funcional RF-05.
 */
public enum TipoEmocion {
    FELICIDAD("Felicidad", "#10B981", "#D1FAE5"),
    NEUTRALIDAD("Neutralidad", "#F59E0B", "#FEF3C7"),
    PREOCUPACION("Preocupación", "#F97316", "#FFEDD5"),
    ENFADO("Enfado", "#EF4444", "#FEE2E2"),
    ANSIEDAD("Ansiedad", "#8B5CF6", "#EDE9FE");

    private final String etiquetaVisible;
    private final String codigoHexColor;
    private final String codigoHexFondo;

    TipoEmocion(String etiquetaVisible, String codigoHexColor, String codigoHexFondo) {
        this.etiquetaVisible = etiquetaVisible;
        this.codigoHexColor = codigoHexColor;
        this.codigoHexFondo = codigoHexFondo;
    }

    public String getEtiquetaVisible() {
        return etiquetaVisible;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public String getCodigoHexFondo() {
        return codigoHexFondo;
    }
}
