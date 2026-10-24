package com.almara.modules.admin.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de la bitácora de auditoría para operaciones del Administrador (HU-11 a HU-15, RNF-13).
 */
@Entity
@Table(name = "auditoria_admin")
public class AuditoriaAdmin {

    @Id
    @Column(name = "id_auditoria")
    private UUID idAuditoria;

    @Column(name = "id_admin")
    private UUID idAdmin;

    @Column(name = "accion", length = 60, nullable = false)
    private String accion;

    @Column(name = "detalle", length = 255, nullable = false)
    private String detalle;

    @Column(name = "actor_responsable", length = 100, nullable = false)
    private String actorResponsable;

    @Column(name = "ip_origen", length = 60)
    private String ipOrigen;

    @Column(name = "fecha_evento", nullable = false)
    private LocalDateTime fechaEvento = LocalDateTime.now();

    public AuditoriaAdmin() {
    }

    public AuditoriaAdmin(UUID idAuditoria, UUID idAdmin, String accion, String detalle,
                          String actorResponsable, String ipOrigen) {
        this.idAuditoria = idAuditoria != null ? idAuditoria : UUID.randomUUID();
        this.idAdmin = idAdmin;
        this.accion = accion;
        this.detalle = detalle;
        this.actorResponsable = actorResponsable;
        this.ipOrigen = ipOrigen;
        this.fechaEvento = LocalDateTime.now();
    }

    public static AuditoriaAdmin crear(UUID idAdmin, String accion, String detalle,
                                       String actorResponsable, String ipOrigen) {
        return new AuditoriaAdmin(UUID.randomUUID(), idAdmin, accion, detalle, actorResponsable, ipOrigen);
    }

    public UUID getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(UUID idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public UUID getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(UUID idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public String getActorResponsable() {
        return actorResponsable;
    }

    public void setActorResponsable(String actorResponsable) {
        this.actorResponsable = actorResponsable;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
