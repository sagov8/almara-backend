package com.almara.modules.emotion.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) para la entrada de indicación de intensidad de una emoción (HU-02).
 * Encapsula exclusivamente los atributos estrictamente necesarios (Patrón DTO arquitectónico).
 * Vincula el nivel de intensidad (escala 1 a 5) al evento previo generado en HU-01.
 */
public class RegistroIntensidadSolicitud {

    private UUID idEvento;

    @NotNull(message = "El nivel de intensidad es obligatorio")
    @Min(value = 1, message = "El nivel de intensidad mínimo permitido es 1 (Leve)")
    @Max(value = 5, message = "El nivel de intensidad máximo permitido es 5 (Intenso)")
    private Integer nivelIntensidad;

    public RegistroIntensidadSolicitud() {
    }

    public RegistroIntensidadSolicitud(UUID idEvento, Integer nivelIntensidad) {
        this.idEvento = idEvento;
        this.nivelIntensidad = nivelIntensidad;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public UUID getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(UUID idEvento) {
        this.idEvento = idEvento;
    }

    public Integer getNivelIntensidad() {
        return nivelIntensidad;
    }

    public void setNivelIntensidad(Integer nivelIntensidad) {
        this.nivelIntensidad = nivelIntensidad;
    }

    @Override
    public String toString() {
        return "RegistroIntensidadSolicitud{" +
                "idEvento=" + idEvento +
                ", nivelIntensidad=" + nivelIntensidad +
                '}';
    }

    public static class Constructor {
        private UUID idEvento;
        private Integer nivelIntensidad;

        public Constructor idEvento(UUID idEvento) {
            this.idEvento = idEvento;
            return this;
        }

        public Constructor nivelIntensidad(Integer nivelIntensidad) {
            this.nivelIntensidad = nivelIntensidad;
            return this;
        }

        public RegistroIntensidadSolicitud build() {
            return new RegistroIntensidadSolicitud(idEvento, nivelIntensidad);
        }
    }
}
