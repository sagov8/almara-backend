package com.almara.modules.admin.services;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.dto.ActualizarResolucionH3Solicitud;
import com.almara.modules.admin.dto.ConfiguracionEspacialH3Respuesta;
import com.almara.modules.admin.dto.CrearPoligonoUrbanoSolicitud;
import com.almara.modules.admin.dto.ModificarPoligonoUrbanoSolicitud;
import com.almara.modules.admin.dto.PoligonoUrbanoRespuesta;
import com.almara.modules.admin.models.ConfiguracionEspacialH3;
import com.almara.modules.admin.models.PoligonoUrbanoEspacial;
import com.almara.modules.admin.patterns.adapter.AdaptadorEspacialPostgisH3;
import com.almara.modules.admin.repositories.RepositorioConfiguracionEspacialH3;
import com.almara.modules.admin.repositories.RepositorioPoligonoUrbanoEspacial;
import com.almara.modules.admin.security.ClaimsTokenAdmin;
import com.almara.modules.geo.models.CoordenadasGps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión administrativa de resolución H3 y polígonos espaciales (HU-14).
 * Integra el patrón Adapter con PostGIS y Uber H3 Core, garantizando la trazabilidad en auditoría.
 */
@Service
public class ServicioGestionZonasEspacialesAdmin {

    private static final Logger log = LoggerFactory.getLogger(ServicioGestionZonasEspacialesAdmin.class);

    private final RepositorioConfiguracionEspacialH3 repositorioConfiguracion;
    private final RepositorioPoligonoUrbanoEspacial repositorioPoligonos;
    private final AdaptadorEspacialPostgisH3 adaptadorEspacial;
    private final ServicioAuditoriaAdmin servicioAuditoriaAdmin;

    public ServicioGestionZonasEspacialesAdmin(RepositorioConfiguracionEspacialH3 repositorioConfiguracion,
                                              RepositorioPoligonoUrbanoEspacial repositorioPoligonos,
                                              AdaptadorEspacialPostgisH3 adaptadorEspacial,
                                              ServicioAuditoriaAdmin servicioAuditoriaAdmin) {
        this.repositorioConfiguracion = repositorioConfiguracion;
        this.repositorioPoligonos = repositorioPoligonos;
        this.adaptadorEspacial = adaptadorEspacial;
        this.servicioAuditoriaAdmin = servicioAuditoriaAdmin;
    }

    /**
     * Consulta la configuración de resolución espacial H3 vigente.
     */
    @Transactional(readOnly = true)
    public ConfiguracionEspacialH3Respuesta obtenerConfiguracionResolucionH3() {
        ConfiguracionEspacialH3 config = repositorioConfiguracion.obtenerConfiguracionGlobal()
                .orElseGet(() -> new ConfiguracionEspacialH3(
                        ConfiguracionEspacialH3.ID_CONFIG_GLOBAL,
                        ConfiguracionEspacialH3.RESOLUCION_DEFECTO,
                        ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA,
                        ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA,
                        "Resolución espacial H3 por defecto para Popayán",
                        LocalDateTime.now(),
                        "SISTEMA"
                ));
        return ConfiguracionEspacialH3Respuesta.fromEntidad(config);
    }

    /**
     * Actualiza la resolución H3 por defecto del sistema (rango 7 a 10, respetando RF-09).
     * Caso de uso incluido: <<include>> Registrar Log de Auditoría.
     */
    @Transactional
    public ConfiguracionEspacialH3Respuesta actualizarResolucionH3Default(
            ActualizarResolucionH3Solicitud solicitud,
            ClaimsTokenAdmin admin,
            String ipOrigen
    ) {
        int resNueva = solicitud.resolucionH3Default();
        if (resNueva < ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA ||
            resNueva > ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "RESOLUCION_H3_FUERA_DE_RANGO",
                    String.format("La resolución espacial H3 debe estar entre %d y %d para proteger la privacidad ciudadana (RF-09).",
                            ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA,
                            ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA)
            );
        }

        ConfiguracionEspacialH3 config = repositorioConfiguracion.obtenerConfiguracionGlobal()
                .orElseGet(() -> new ConfiguracionEspacialH3(
                        ConfiguracionEspacialH3.ID_CONFIG_GLOBAL,
                        ConfiguracionEspacialH3.RESOLUCION_DEFECTO,
                        ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA,
                        ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA,
                        "Resolución espacial H3 por defecto para Popayán",
                        LocalDateTime.now(),
                        admin.getCorreo()
                ));

        int resAnterior = config.getResolucionH3Default();
        config.setResolucionH3Default(resNueva);
        config.setFechaActualizacion(LocalDateTime.now());
        config.setActualizadoPor(admin.getCorreo());

        ConfiguracionEspacialH3 guardada = repositorioConfiguracion.save(config);

        // Registro de Auditoría
        String detalle = String.format("Ajustada resolución H3 por defecto de res=%d a res=%d. Motivo: %s",
                resAnterior, resNueva, solicitud.justificacionAuditoria().trim());

        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdAdmin(),
                "H3_RESOLUCION_ACTUALIZADA",
                detalle,
                admin.getCorreo(),
                ipOrigen
        );

        log.info("Resolución espacial H3 actualizada por admin='{}': anterior={}, nueva={}",
                admin.getCorreo(), resAnterior, resNueva);

        return ConfiguracionEspacialH3Respuesta.fromEntidad(guardada);
    }

    /**
     * Lista todos los polígonos espaciales urbanos registrados.
     */
    @Transactional(readOnly = true)
    public List<PoligonoUrbanoRespuesta> listarPoligonosUrbanos() {
        return repositorioPoligonos.findAllByOrderByNombreZonaAsc().stream()
                .map(PoligonoUrbanoRespuesta::fromEntidad)
                .toList();
    }

    /**
     * Obtiene el detalle de un polígono urbano por su identificador.
     */
    @Transactional(readOnly = true)
    public PoligonoUrbanoRespuesta obtenerPoligonoPorId(String idZona) {
        return repositorioPoligonos.findByIdZonaIgnoreCase(idZona)
                .map(PoligonoUrbanoRespuesta::fromEntidad)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "POLIGONO_NO_ENCONTRADO",
                        "No se encontró el polígono urbano con identificador: " + idZona
                ));
    }

    /**
     * Registra un nuevo polígono urbano validando la geometría GeoJSON cerrada y calculando su indexación espacial.
     * Caso de uso incluido: <<include>> Registrar Log de Auditoría.
     */
    @Transactional
    public PoligonoUrbanoRespuesta crearPoligonoUrbano(
            CrearPoligonoUrbanoSolicitud solicitud,
            ClaimsTokenAdmin admin,
            String ipOrigen
    ) {
        String idNormalizado = solicitud.idZona().trim().toUpperCase();

        if (repositorioPoligonos.existsByIdZonaIgnoreCase(idNormalizado)) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.CONFLICT,
                    "ZONA_DUPLICADA",
                    "Ya existe un polígono urbano registrado con el identificador: " + idNormalizado
            );
        }

        if (!adaptadorEspacial.validarGeoJsonPoligonoCerrado(solicitud.geometriaGeoJson())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "GEOJSON_INVALIDO",
                    "La geometría debe ser un polígono GeoJSON cerrado válido (RFC 7946 / PostGIS OGC)."
            );
        }

        int resolucion = solicitud.resolucionH3() != null ? solicitud.resolucionH3() : ConfiguracionEspacialH3.RESOLUCION_DEFECTO;
        if (resolucion < ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA ||
            resolucion > ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "RESOLUCION_H3_FUERA_DE_RANGO",
                    "La resolución H3 debe encontrarse entre 7 y 10 (RF-09)."
            );
        }

        CoordenadasGps centroide = adaptadorEspacial.calcularCentroide(solicitud.geometriaGeoJson());
        String idCeldaCentroide = adaptadorEspacial.centroideACeldaH3(solicitud.geometriaGeoJson(), resolucion);
        int celdasCubiertas = adaptadorEspacial.estimarCeldasCubiertas(solicitud.geometriaGeoJson(), resolucion);

        PoligonoUrbanoEspacial entidad = new PoligonoUrbanoEspacial(
                idNormalizado,
                solicitud.nombreZona().trim(),
                solicitud.descripcion() != null ? solicitud.descripcion().trim() : null,
                solicitud.geometriaGeoJson().trim(),
                centroide.getLatitud(),
                centroide.getLongitud(),
                resolucion,
                idCeldaCentroide,
                celdasCubiertas,
                true,
                admin.getCorreo()
        );

        PoligonoUrbanoEspacial guardado = repositorioPoligonos.save(entidad);

        // Registro de Auditoría
        String detalle = String.format("Creado polígono urbano [%s] '%s' (centroide: %.5f, %.5f, celda H3: %s). Justificación: %s",
                idNormalizado, entidad.getNombreZona(), centroide.getLatitud(), centroide.getLongitud(),
                idCeldaCentroide, solicitud.justificacionAuditoria().trim());

        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdAdmin(),
                "POLIGONO_URBANO_CREADO",
                detalle,
                admin.getCorreo(),
                ipOrigen
        );

        log.info("Polígono urbano creado exitosamente por admin='{}': id='{}'", admin.getCorreo(), idNormalizado);
        return PoligonoUrbanoRespuesta.fromEntidad(guardado);
    }

    /**
     * Modifica un polígono urbano existente recalculando su centroide e indexación H3.
     */
    @Transactional
    public PoligonoUrbanoRespuesta modificarPoligonoUrbano(
            String idZona,
            ModificarPoligonoUrbanoSolicitud solicitud,
            ClaimsTokenAdmin admin,
            String ipOrigen
    ) {
        PoligonoUrbanoEspacial entidad = repositorioPoligonos.findByIdZonaIgnoreCase(idZona)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "POLIGONO_NO_ENCONTRADO",
                        "No se encontró el polígono urbano con identificador: " + idZona
                ));

        if (!adaptadorEspacial.validarGeoJsonPoligonoCerrado(solicitud.geometriaGeoJson())) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "GEOJSON_INVALIDO",
                    "La geometría debe ser un polígono GeoJSON cerrado válido (RFC 7946 / PostGIS OGC)."
            );
        }

        int resolucion = solicitud.resolucionH3() != null ? solicitud.resolucionH3() : entidad.getResolucionH3();
        if (resolucion < ConfiguracionEspacialH3.RESOLUCION_MINIMA_PERMITIDA ||
            resolucion > ConfiguracionEspacialH3.RESOLUCION_MAXIMA_PERMITIDA) {
            throw new ExcepcionReglaNegocioInstitucional(
                    HttpStatus.BAD_REQUEST,
                    "RESOLUCION_H3_FUERA_DE_RANGO",
                    "La resolución H3 debe encontrarse entre 7 y 10 (RF-09)."
            );
        }

        CoordenadasGps centroide = adaptadorEspacial.calcularCentroide(solicitud.geometriaGeoJson());
        String idCeldaCentroide = adaptadorEspacial.centroideACeldaH3(solicitud.geometriaGeoJson(), resolucion);
        int celdasCubiertas = adaptadorEspacial.estimarCeldasCubiertas(solicitud.geometriaGeoJson(), resolucion);

        entidad.setNombreZona(solicitud.nombreZona().trim());
        if (solicitud.descripcion() != null) {
            entidad.setDescripcion(solicitud.descripcion().trim());
        }
        entidad.setGeometriaGeoJson(solicitud.geometriaGeoJson().trim());
        entidad.setLatitudCentroide(centroide.getLatitud());
        entidad.setLongitudCentroide(centroide.getLongitud());
        entidad.setResolucionH3(resolucion);
        entidad.setIdCeldaH3Centroide(idCeldaCentroide);
        entidad.setTotalCeldasH3Cubiertas(celdasCubiertas);
        if (solicitud.activo() != null) {
            entidad.setActivo(solicitud.activo());
        }
        entidad.setActualizadoPor(admin.getCorreo());

        PoligonoUrbanoEspacial guardado = repositorioPoligonos.save(entidad);

        // Registro de Auditoría
        String detalle = String.format("Modificado polígono urbano [%s] '%s'. Justificación: %s",
                entidad.getIdZona(), entidad.getNombreZona(), solicitud.justificacionAuditoria().trim());

        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdAdmin(),
                "POLIGONO_URBANO_MODIFICADO",
                detalle,
                admin.getCorreo(),
                ipOrigen
        );

        log.info("Polígono urbano modificado por admin='{}': id='{}'", admin.getCorreo(), entidad.getIdZona());
        return PoligonoUrbanoRespuesta.fromEntidad(guardado);
    }

    /**
     * Desactiva (borrado lógico) un polígono urbano en la base espacial.
     */
    @Transactional
    public PoligonoUrbanoRespuesta desactivarPoligonoUrbano(
            String idZona,
            String justificacion,
            ClaimsTokenAdmin admin,
            String ipOrigen
    ) {
        PoligonoUrbanoEspacial entidad = repositorioPoligonos.findByIdZonaIgnoreCase(idZona)
                .orElseThrow(() -> new ExcepcionReglaNegocioInstitucional(
                        HttpStatus.NOT_FOUND,
                        "POLIGONO_NO_ENCONTRADO",
                        "No se encontró el polígono urbano con identificador: " + idZona
                ));

        entidad.setActivo(false);
        entidad.setActualizadoPor(admin.getCorreo());
        PoligonoUrbanoEspacial guardado = repositorioPoligonos.save(entidad);

        // Registro de Auditoría
        String detalle = String.format("Desactivado polígono urbano [%s] '%s'. Motivo: %s",
                entidad.getIdZona(), entidad.getNombreZona(),
                justificacion != null && !justificacion.isBlank() ? justificacion.trim() : "Desactivación administrativa");

        servicioAuditoriaAdmin.registrarEvento(
                admin.getIdAdmin(),
                "POLIGONO_URBANO_DESACTIVADO",
                detalle,
                admin.getCorreo(),
                ipOrigen
        );

        log.info("Polígono urbano desactivado por admin='{}': id='{}'", admin.getCorreo(), idZona);
        return PoligonoUrbanoRespuesta.fromEntidad(guardado);
    }
}
