package com.almara.modules.admin.services;

import com.almara.modules.admin.models.AuditoriaAdmin;
import com.almara.modules.admin.repositories.RepositorioAuditoriaAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio centralizado para registrar eventos en la bitácora de auditoría administrativa (RNF-13, HU-11 a HU-15).
 */
@Service
public class ServicioAuditoriaAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioAuditoriaAdmin.class);

    private final RepositorioAuditoriaAdmin repositorioAuditoriaAdmin;

    public ServicioAuditoriaAdmin(RepositorioAuditoriaAdmin repositorioAuditoriaAdmin) {
        this.repositorioAuditoriaAdmin = repositorioAuditoriaAdmin;
    }

    /**
     * Registra un evento de auditoría en una transacción independiente para preservar la traza
     * incluso si la operación del negocio falla o se revierte.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaAdmin registrarEvento(UUID idAdmin, String accion, String detalle,
                                         String actorResponsable, String ipOrigen) {
        AuditoriaAdmin evento = AuditoriaAdmin.crear(idAdmin, accion, detalle, actorResponsable, ipOrigen);
        AuditoriaAdmin guardado = repositorioAuditoriaAdmin.save(evento);
        log.info("[AUDITORIA ADMIN] accion='{}', actor='{}', idAdmin='{}', detalle='{}'",
                accion, actorResponsable, idAdmin, detalle);
        return guardado;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaAdmin> listarUltimosEventos() {
        return repositorioAuditoriaAdmin.findTop50ByOrderByFechaEventoDesc();
    }
}
