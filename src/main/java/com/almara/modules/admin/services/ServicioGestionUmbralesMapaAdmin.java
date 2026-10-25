package com.almara.modules.admin.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.ActualizarUmbralMapaSolicitud;
import com.almara.modules.admin.dto.ConfiguracionUmbralMapaRespuesta;
import com.almara.modules.admin.models.ConfiguracionMapaUmbral;
import com.almara.modules.admin.repositories.RepositorioConfiguracionMapaUmbral;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de negocio para la gestión y calibración en caliente de los umbrales
 * del mapa emocional y reglas de k-anonimato (HU-13).
 */
@Service
public class ServicioGestionUmbralesMapaAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioGestionUmbralesMapaAdmin.class);

    private final RepositorioConfiguracionMapaUmbral repositorio;
    private final ProveedorConfiguracionUmbralMapa proveedorConfiguracion;
    private final ServicioAuditoriaAdmin servicioAuditoriaAdmin;

    public ServicioGestionUmbralesMapaAdmin(RepositorioConfiguracionMapaUmbral repositorio,
                                            ProveedorConfiguracionUmbralMapa proveedorConfiguracion,
                                            ServicioAuditoriaAdmin servicioAuditoriaAdmin) {
        this.repositorio = repositorio;
        this.proveedorConfiguracion = proveedorConfiguracion;
        this.servicioAuditoriaAdmin = servicioAuditoriaAdmin;
    }

    /**
     * Obtiene la configuración de umbrales activa en el sistema.
     */
    @Transactional(readOnly = true)
    public ConfiguracionUmbralMapaRespuesta obtenerConfiguracion() {
        ConfiguracionMapaUmbral config = proveedorConfiguracion.obtenerConfiguracionActual();
        return ConfiguracionUmbralMapaRespuesta.fromEntidad(config);
    }

    /**
     * Actualiza en caliente los umbrales de privacidad k-anonimato del mapa (HU-13).
     * Incluye validación de seguridad (k >= 5) y registro de log de auditoría.
     */
    @Transactional
    public ConfiguracionUmbralMapaRespuesta actualizarConfiguracion(ActualizarUmbralMapaSolicitud solicitud,
                                                                  ClaimsTokenAdmin admin,
                                                                  String ipOrigen) {
        if (solicitud.umbralMinimoK() < ConfiguracionMapaUmbral.LIMITE_MINIMO_TECNICO_K) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "UMBRAL_K_INSEGURO",
                    String.format("El umbral de k-anonimato no puede ser inferior a %d para proteger la privacidad ciudadana (RF-23).",
                            ConfiguracionMapaUmbral.LIMITE_MINIMO_TECNICO_K)
            );
        }

        if (solicitud.tiempoRecalculoSegundos() < ConfiguracionMapaUmbral.TIEMPO_RECALCULO_MINIMO_SEGUNDOS) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "TIEMPO_RECALCULO_INVALIDO",
                    String.format("El tiempo de recálculo no puede ser inferior a %d segundos.",
                            ConfiguracionMapaUmbral.TIEMPO_RECALCULO_MINIMO_SEGUNDOS)
            );
        }

        ConfiguracionMapaUmbral entidad = repositorio.obtenerConfiguracionGlobal()
                .orElseGet(() -> new ConfiguracionMapaUmbral(
                        ConfiguracionMapaUmbral.ID_CONFIGURACION_GLOBAL,
                        5,
                        60,
                        true,
                        "Protección en Comunidad: Para cuidar tu privacidad, las zonas solo se colorean en el mapa cuando al menos 5 vecinos han compartido cómo se sienten en ese lugar.",
                        LocalDateTime.now(),
                        admin.getCorreo()
                ));

        int previoK = entidad.getUmbralMinimoK();
        int nuevoK = solicitud.umbralMinimoK();
        int previoTiempo = entidad.getTiempoRecalculoSegundos();
        int nuevoTiempo = solicitud.tiempoRecalculoSegundos();

        entidad.setUmbralMinimoK(nuevoK);
        entidad.setTiempoRecalculoSegundos(nuevoTiempo);
        if (solicitud.modoEstricto() != null) {
            entidad.setModoEstricto(solicitud.modoEstricto());
        }
        if (solicitud.mensajeProteccion() != null && !solicitud.mensajeProteccion().isBlank()) {
            entidad.setMensajeProteccion(solicitud.mensajeProteccion().trim());
        }
        entidad.setFechaActualizacion(LocalDateTime.now());
        entidad.setActualizadoPor(admin.getCorreo());

        ConfiguracionMapaUmbral guardada = repositorio.save(entidad);

        // Propagación en caliente al motor de agregación (Observer Pattern)
        proveedorConfiguracion.propagarNuevaConfiguracion(guardada);

        // Caso de uso incluido: Registrar Log de Auditoría
        String detalle = String.format("Ajustado umbral k-anonimato de k=%d a k=%d (recalculo: %ds -> %ds). Motivo: %s",
                previoK, nuevoK, previoTiempo, nuevoTiempo, solicitud.justificacionAuditoria().trim());

        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdAdmin(),
                "UMBRAL_MAPA_ACTUALIZADO",
                detalle,
                admin.getCorreo(),
                ipOrigen
        );

        log.info("Umbrales del mapa actualizados exitosamente por admin='{}': previoK={}, nuevoK={}",
                admin.getCorreo(), previoK, nuevoK);

        return ConfiguracionUmbralMapaRespuesta.fromEntidad(guardada);
    }

    /**
     * Restablece los umbrales y políticas a los valores institucionales por defecto.
     */
    @Transactional
    public ConfiguracionUmbralMapaRespuesta restablecerPorDefecto(ClaimsTokenAdmin admin,
                                                                String justificacion,
                                                                String ipOrigen) {
        ActualizarUmbralMapaSolicitud solicitudDefecto = new ActualizarUmbralMapaSolicitud(
                5,
                60,
                true,
                "Protección en Comunidad: Para cuidar tu privacidad, las zonas solo se colorean en el mapa cuando al menos 5 vecinos han compartido cómo se sienten en ese lugar. Tu participación es 100% anónima.",
                justificacion != null && !justificacion.isBlank() ? justificacion : "Restablecimiento administrativo a parámetros base por defecto de Popayán"
        );

        return actualizarConfiguracion(solicitudDefecto, admin, ipOrigen);
    }
}
