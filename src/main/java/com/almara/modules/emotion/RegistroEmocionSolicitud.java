package com.almara.modules.emotion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) para la entrada de selección de emoción ciudadana.
 * Garantiza que únicamente viajen los campos permitidos sin datos personales identificables.
 */
public class RegistroEmocionSolicitud {

    @NotNull(message = "La emoción seleccionada es obligatoria y debe pertenecer al catálogo predeterminado")
    private TipoEmocion emocion;

    @NotBlank(message = "El identificador de sesión anónimo (token_sesion_temporal) es obligatorio")
    @Size(min = 10, max = 64, message = "El token de sesión temporal debe tener entre 10 y 64 caracteres")
    private String tokenSesionTemporal;

    public RegistroEmocionSolicitud() {
    }

    public RegistroEmocionSolicitud(TipoEmocion emocion, String tokenSesionTemporal) {
        this.emocion = emocion;
        this.tokenSesionTemporal = tokenSesionTemporal;
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

    public static class Constructor {
        private TipoEmocion emocion;
        private String tokenSesionTemporal;

        public Constructor emocion(TipoEmocion emocion) {
            this.emocion = emocion;
            return this;
        }

        public Constructor tokenSesionTemporal(String tokenSesionTemporal) {
            this.tokenSesionTemporal = tokenSesionTemporal;
            return this;
        }

        public RegistroEmocionSolicitud build() {
            return new RegistroEmocionSolicitud(emocion, tokenSesionTemporal);
        }
    }
}
