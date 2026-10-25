package com.almara.modules.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidad JPA que almacena la configuración de umbrales del mapa y políticas
 * de privacidad ($k$-anonimato) ajustadas por el Administrador (HU-13).
 */
@Entity
@Table(name = "configuracion_mapa_umbral")
public class ConfiguracionMapaUmbral {

    public static final String ID_CONFIGURACION_GLOBAL = "GLOBAL_POPAYAN";
    public static final int LIMITE_MINIMO_TECNICO_K = 5;
    public static final int TIEMPO_RECALCULO_MINIMO_SEGUNDOS = 10;

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "umbral_minimo_k", nullable = false)
    private int umbralMinimoK;

    @Column(name = "tiempo_recalculo_segundos", nullable = false)
    private int tiempoRecalculoSegundos;

    @Column(name = "modo_estricto", nullable = false)
    private boolean modoEstricto;

    @Column(name = "mensaje_proteccion", length = 500, nullable = false)
    private String mensajeProteccion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por", length = 100, nullable = false)
    private String actualizadoPor;

    public ConfiguracionMapaUmbral() {
    }

    public ConfiguracionMapaUmbral(String id, int umbralMinimoK, int tiempoRecalculoSegundos,
                                   boolean modoEstricto, String mensajeProteccion,
                                   LocalDateTime fechaActualizacion, String actualizadoPor) {
        this.id = id;
        this.umbralMinimoK = umbralMinimoK;
        this.tiempoRecalculoSegundos = tiempoRecalculoSegundos;
        this.modoEstricto = modoEstricto;
        this.mensajeProteccion = mensajeProteccion;
        this.fechaActualizacion = fechaActualizacion;
        this.actualizadoPor = actualizadoPor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUmbralMinimoK() {
        return umbralMinimoK;
    }

    public void setUmbralMinimoK(int umbralMinimoK) {
        this.umbralMinimoK = umbralMinimoK;
    }

    public int getTiempoRecalculoSegundos() {
        return tiempoRecalculoSegundos;
    }

    public void setTiempoRecalculoSegundos(int tiempoRecalculoSegundos) {
        this.tiempoRecalculoSegundos = tiempoRecalculoSegundos;
    }

    public boolean isModoEstricto() {
        return modoEstricto;
    }

    public void setModoEstricto(boolean modoEstricto) {
        this.modoEstricto = modoEstricto;
    }

    public String getMensajeProteccion() {
        return mensajeProteccion;
    }

    public void setMensajeProteccion(String mensajeProteccion) {
        this.mensajeProteccion = mensajeProteccion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getActualizadoPor() {
        return actualizadoPor;
    }

    public void setActualizadoPor(String actualizadoPor) {
        this.actualizadoPor = actualizadoPor;
    }
}
