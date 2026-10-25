package com.almara.modules.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Entidad JPA para la persistencia de la configuración de resolución espacial H3 (HU-14).
 * Define la resolución por defecto y límites operativos (7 a 10) respetando RF-09.
 */
@Entity
@Table(name = "configuracion_espacial_h3")
public class ConfiguracionEspacialH3 {

    public static final String ID_CONFIG_GLOBAL = "GLOBAL_POPAYAN";
    public static final int RESOLUCION_MINIMA_PERMITIDA = 7;
    public static final int RESOLUCION_MAXIMA_PERMITIDA = 10;
    public static final int RESOLUCION_DEFECTO = 9;

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "resolucion_h3_default", nullable = false)
    private int resolucionH3Default;

    @Column(name = "resolucion_minima", nullable = false)
    private int resolucionMinima;

    @Column(name = "resolucion_maxima", nullable = false)
    private int resolucionMaxima;

    @Column(name = "descripcion", length = 255, nullable = false)
    private String descripcion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "actualizado_por", length = 100, nullable = false)
    private String actualizadoPor;

    public ConfiguracionEspacialH3() {
    }

    public ConfiguracionEspacialH3(String id, int resolucionH3Default, int resolucionMinima,
                                  int resolucionMaxima, String descripcion,
                                  LocalDateTime fechaActualizacion, String actualizadoPor) {
        this.id = id;
        this.resolucionH3Default = resolucionH3Default;
        this.resolucionMinima = resolucionMinima;
        this.resolucionMaxima = resolucionMaxima;
        this.descripcion = descripcion;
        this.fechaActualizacion = fechaActualizacion;
        this.actualizadoPor = actualizadoPor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getResolucionH3Default() {
        return resolucionH3Default;
    }

    public void setResolucionH3Default(int resolucionH3Default) {
        this.resolucionH3Default = resolucionH3Default;
    }

    public int getResolucionMinima() {
        return resolucionMinima;
    }

    public void setResolucionMinima(int resolucionMinima) {
        this.resolucionMinima = resolucionMinima;
    }

    public int getResolucionMaxima() {
        return resolucionMaxima;
    }

    public void setResolucionMaxima(int resolucionMaxima) {
        this.resolucionMaxima = resolucionMaxima;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
