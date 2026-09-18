package com.almara.modules.aggregation.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA mapeada a la tabla estado_emocional_zona (HU-05, HU-07, HU-16).
 * Almacena el snapshot agregado del clima emocional de cada celda H3,
 * garantizando la persistencia para análisis histórico e institucional.
 */
@Entity
@Table(name = "estado_emocional_zona")
public class EntidadEstadoEmocionalZona {

    @Id
    @Column(name = "id_celda_h3", length = 15, nullable = false)
    private String idCeldaH3;

    @Column(name = "emocion_predominante", length = 20)
    private String emocionPredominante;

    @Column(name = "codigo_hex_color", length = 7)
    private String codigoHexColor;

    @Column(name = "icono_svg", length = 255)
    private String iconoSvg;

    @Column(name = "distribucion_porcentual", length = 1000)
    private String distribucionPorcentual;

    @Column(name = "tendencia", length = 50)
    private String tendencia;

    @Column(name = "participantes_aproximados", length = 50)
    private String participantesAproximados;

    @Column(name = "total_eventos_periodo", nullable = false)
    private Integer totalEventosPeriodo;

    @Column(name = "periodo_temporal_metrica", length = 50)
    private String periodoTemporalMetrica;

    @Column(name = "cumple_umbral", nullable = false)
    private Boolean cumpleUmbral;

    @Column(name = "ultima_actualizacion")
    private Instant ultimaActualizacion;

    public EntidadEstadoEmocionalZona() {
    }

    public EntidadEstadoEmocionalZona(String idCeldaH3, String emocionPredominante, String codigoHexColor,
                                      String iconoSvg, String distribucionPorcentual, String tendencia,
                                      String participantesAproximados, Integer totalEventosPeriodo,
                                      String periodoTemporalMetrica, Boolean cumpleUmbral, Instant ultimaActualizacion) {
        this.idCeldaH3 = idCeldaH3;
        this.emocionPredominante = emocionPredominante;
        this.codigoHexColor = codigoHexColor;
        this.iconoSvg = iconoSvg;
        this.distribucionPorcentual = distribucionPorcentual;
        this.tendencia = tendencia;
        this.participantesAproximados = participantesAproximados;
        this.totalEventosPeriodo = totalEventosPeriodo != null ? totalEventosPeriodo : 0;
        this.periodoTemporalMetrica = periodoTemporalMetrica;
        this.cumpleUmbral = cumpleUmbral != null ? cumpleUmbral : false;
        this.ultimaActualizacion = ultimaActualizacion != null ? ultimaActualizacion : Instant.now();
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public String getIdCeldaH3() {
        return idCeldaH3;
    }

    public void setIdCeldaH3(String idCeldaH3) {
        this.idCeldaH3 = idCeldaH3;
    }

    public String getEmocionPredominante() {
        return emocionPredominante;
    }

    public void setEmocionPredominante(String emocionPredominante) {
        this.emocionPredominante = emocionPredominante;
    }

    public String getCodigoHexColor() {
        return codigoHexColor;
    }

    public void setCodigoHexColor(String codigoHexColor) {
        this.codigoHexColor = codigoHexColor;
    }

    public String getIconoSvg() {
        return iconoSvg;
    }

    public void setIconoSvg(String iconoSvg) {
        this.iconoSvg = iconoSvg;
    }

    public String getDistribucionPorcentual() {
        return distribucionPorcentual;
    }

    public void setDistribucionPorcentual(String distribucionPorcentual) {
        this.distribucionPorcentual = distribucionPorcentual;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public String getParticipantesAproximados() {
        return participantesAproximados;
    }

    public void setParticipantesAproximados(String participantesAproximados) {
        this.participantesAproximados = participantesAproximados;
    }

    public Integer getTotalEventosPeriodo() {
        return totalEventosPeriodo;
    }

    public void setTotalEventosPeriodo(Integer totalEventosPeriodo) {
        this.totalEventosPeriodo = totalEventosPeriodo;
    }

    public String getPeriodoTemporalMetrica() {
        return periodoTemporalMetrica;
    }

    public void setPeriodoTemporalMetrica(String periodoTemporalMetrica) {
        this.periodoTemporalMetrica = periodoTemporalMetrica;
    }

    public Boolean getCumpleUmbral() {
        return cumpleUmbral;
    }

    public void setCumpleUmbral(Boolean cumpleUmbral) {
        this.cumpleUmbral = cumpleUmbral;
    }

    public Instant getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(Instant ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public static class Constructor {
        private String idCeldaH3;
        private String emocionPredominante;
        private String codigoHexColor;
        private String iconoSvg;
        private String distribucionPorcentual;
        private String tendencia;
        private String participantesAproximados;
        private Integer totalEventosPeriodo = 0;
        private String periodoTemporalMetrica;
        private Boolean cumpleUmbral = false;
        private Instant ultimaActualizacion = Instant.now();

        public Constructor idCeldaH3(String idCeldaH3) {
            this.idCeldaH3 = idCeldaH3;
            return this;
        }

        public Constructor emocionPredominante(String emocionPredominante) {
            this.emocionPredominante = emocionPredominante;
            return this;
        }

        public Constructor codigoHexColor(String codigoHexColor) {
            this.codigoHexColor = codigoHexColor;
            return this;
        }

        public Constructor iconoSvg(String iconoSvg) {
            this.iconoSvg = iconoSvg;
            return this;
        }

        public Constructor distribucionPorcentual(String distribucionPorcentual) {
            this.distribucionPorcentual = distribucionPorcentual;
            return this;
        }

        public Constructor tendencia(String tendencia) {
            this.tendencia = tendencia;
            return this;
        }

        public Constructor participantesAproximados(String participantesAproximados) {
            this.participantesAproximados = participantesAproximados;
            return this;
        }

        public Constructor totalEventosPeriodo(Integer totalEventosPeriodo) {
            this.totalEventosPeriodo = totalEventosPeriodo;
            return this;
        }

        public Constructor periodoTemporalMetrica(String periodoTemporalMetrica) {
            this.periodoTemporalMetrica = periodoTemporalMetrica;
            return this;
        }

        public Constructor cumpleUmbral(Boolean cumpleUmbral) {
            this.cumpleUmbral = cumpleUmbral;
            return this;
        }

        public Constructor ultimaActualizacion(Instant ultimaActualizacion) {
            this.ultimaActualizacion = ultimaActualizacion;
            return this;
        }

        public EntidadEstadoEmocionalZona build() {
            return new EntidadEstadoEmocionalZona(idCeldaH3, emocionPredominante, codigoHexColor, iconoSvg,
                    distribucionPorcentual, tendencia, participantesAproximados, totalEventosPeriodo,
                    periodoTemporalMetrica, cumpleUmbral, ultimaActualizacion);
        }
    }
}
