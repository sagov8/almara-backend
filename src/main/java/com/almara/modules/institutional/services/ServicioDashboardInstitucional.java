package com.almara.modules.institutional.services;

import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.aggregation.services.ServicioDetalleZona;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.models.ReporteEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocion;
import com.almara.modules.geo.models.Zona;
import com.almara.modules.geo.repositories.RepositorioZona;
import com.almara.modules.institutional.dto.DashboardInstitucionalResumenRespuesta;
import com.almara.modules.institutional.dto.FiltroTerritorialDashboardRespuesta;
import com.almara.modules.institutional.dto.IndicadorEmocionCiudadDto;
import com.almara.modules.institutional.dto.ReporteAnaliticoExportacionRespuesta;
import com.almara.modules.institutional.dto.ZonaTerritorialAnaliticaDto;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.almara.modules.institutional.repositories.RepositorioClienteInstitucional;
import com.almara.modules.institutional.security.ExcepcionModuloRestringidoPorPlan;
import com.almara.modules.institutional.security.ResultadoValidacionModulo;
import com.almara.modules.institutional.security.ValidadorPermisosBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio del panel analítico para clientes institucionales (HU-24, RF-34).
 * Provee resúmenes globales, filtrado territorial y exportación de reportes
 * integrando el control de acceso por plan (HU-20) y privacidad de datos (k >= 5).
 */
@Service
public class ServicioDashboardInstitucional {

    private static final Logger log = LoggerFactory.getLogger(ServicioDashboardInstitucional.class);
    private static final int UMBRAL_K_ANONIMATO = 5;

    private final ValidadorPermisosBackend validadorPermisosBackend;
    private final RepositorioClienteInstitucional repositorioClienteInstitucional;
    private final RepositorioZona repositorioZona;
    private final RepositorioReporteEmocion repositorioReporteEmocion;
    private final RepositorioCatalogoEmocion repositorioCatalogoEmocion;
    private final ServicioAgregacionMapa servicioAgregacionMapa;
    private final ServicioDetalleZona servicioDetalleZona;

    public ServicioDashboardInstitucional(ValidadorPermisosBackend validadorPermisosBackend,
                                         RepositorioClienteInstitucional repositorioClienteInstitucional,
                                         RepositorioZona repositorioZona,
                                         RepositorioReporteEmocion repositorioReporteEmocion,
                                          RepositorioCatalogoEmocion repositorioCatalogoEmocion,
                                         ServicioAgregacionMapa servicioAgregacionMapa,
                                         ServicioDetalleZona servicioDetalleZona) {
        this.validadorPermisosBackend = validadorPermisosBackend;
        this.repositorioClienteInstitucional = repositorioClienteInstitucional;
        this.repositorioZona = repositorioZona;
        this.repositorioReporteEmocion = repositorioReporteEmocion;
        this.repositorioCatalogoEmocion = repositorioCatalogoEmocion;
        this.servicioAgregacionMapa = servicioAgregacionMapa;
        this.servicioDetalleZona = servicioDetalleZona;
    }

    private Map<String, String> obtenerMapaIconosCatalogo() {
        try {
            if (repositorioCatalogoEmocion != null) {
                List<EntidadCatalogoEmocion> catalogo = repositorioCatalogoEmocion.findByActivoTrue();
                if (catalogo != null && !catalogo.isEmpty()) {
                    return catalogo.stream().collect(Collectors.toMap(
                            e -> e.getIdEmocion().toUpperCase(),
                            e -> e.getIconoSvg() != null && !e.getIconoSvg().isBlank() ? e.getIconoSvg().trim() : "Smile",
                            (existente, reemplazo) -> existente
                    ));
                }
            }
        } catch (Exception ex) {
            log.warn("No se pudo cargar el catálogo de iconos de emociones desde la BD: {}", ex.getMessage());
        }
        return Collections.emptyMap();
    }

    /**
     * Consulta el resumen analítico institucional de la ciudad de Popayán (RF-34).
     * Requiere que el plan incluya el módulo MOD_DASHBOARD_BASICO.
     */
    public DashboardInstitucionalResumenRespuesta consultarDashboardResumen(String authorizationHeader, String periodo) {
        long inicioMs = System.currentTimeMillis();

        // 1. Validar acceso al módulo base
        ResultadoValidacionModulo validacion = validadorPermisosBackend
                .validarAccesoModuloPorToken(authorizationHeader, "MOD_DASHBOARD_BASICO");

        ClienteInstitucional cliente = repositorioClienteInstitucional.findById(validacion.getIdInstitucion())
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "CLIENTE_NO_ENCONTRADO",
                        "No se encontró el registro institucional para la entidad autenticada"
                ));

        // 2. Asegurar existencia de reportes agregados
        if (repositorioReporteEmocion.contarTotal() == 0) {
            log.info("Sembrando reportes iniciales para analítica institucional...");
            servicioAgregacionMapa.sembrarReportesIniciales();
        }

        // 3. Evaluar permisos para módulos extendidos
        boolean filtroHabilitado = validadorPermisosBackend.autorizarPeticion(validacion.getIdSuscripcion(), "MOD_FILTROS_TERRITORIALES");
        boolean exportacionHabilitada = validadorPermisosBackend.autorizarPeticion(validacion.getIdSuscripcion(), "MOD_EXPORTACION_REPORTES");

        // 4. Calcular zonas analíticas territoriales
        List<Zona> zonasManuales = repositorioZona.obtenerZonasManuales();
        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();

        List<ZonaTerritorialAnaliticaDto> zonasAnaliticas = calcularZonasAnaliticas(zonasManuales, todosReportes);

        int zonasConDatos = (int) zonasAnaliticas.stream().filter(ZonaTerritorialAnaliticaDto::isCumpleUmbral).count();
        int zonasEnReserva = zonasAnaliticas.size() - zonasConDatos;

        // 5. Calcular métricas agregadas de ciudad
        List<IndicadorEmocionCiudadDto> distribucionGlobal = calcularDistribucionGlobalCiudad(todosReportes);
        TipoEmocion emocionDominanteCiudad = calcularEmocionPredominanteGlobal(todosReportes);
        double intensidadPromedioCiudad = calcularIntensidadPromedioGlobal(todosReportes);

        long duracionMs = System.currentTimeMillis() - inicioMs;

        return DashboardInstitucionalResumenRespuesta.builder()
                .idInstitucion(cliente.getIdUsuario())
                .razonSocial(cliente.getRazonSocial())
                .codigoPlanActivo(validacion.getCodigoPlan())
                .periodoConsultado(periodo != null ? periodo : "ULTIMAS_24_HORAS")
                .totalZonasMonitoreadas(zonasAnaliticas.size())
                .zonasConDatosSuficientes(zonasConDatos)
                .zonasEnReservaPrivacidad(zonasEnReserva)
                .emocionPredominanteCiudad(emocionDominanteCiudad != null ? emocionDominanteCiudad.getEtiquetaVisible() : "Sin datos")
                .intensidadPromedioCiudad(intensidadPromedioCiudad)
                .distribucionGlobalCiudad(distribucionGlobal)
                .resumenZonas(zonasAnaliticas)
                .filtroTerritorialHabilitado(filtroHabilitado)
                .exportacionHabilitada(exportacionHabilitada)
                .tiempoCalculoMs(duracionMs)
                .build();
    }

    /**
     * Filtra el clima emocional por zonas territoriales (<<extend>> MOD_FILTROS_TERRITORIALES).
     * Requiere que el plan incluya el módulo MOD_FILTROS_TERRITORIALES (PLAN_STANDARD o PLAN_ENTERPRISE).
     */
    public FiltroTerritorialDashboardRespuesta filtrarClimaEmocionalPorZonas(String authorizationHeader,
                                                                             String zonaManualId,
                                                                             String idCeldaH3,
                                                                             String emocion,
                                                                             String periodo) {
        long inicioMs = System.currentTimeMillis();

        // 1. Validar permiso sobre módulo premium
        validadorPermisosBackend.validarAccesoModuloPorToken(authorizationHeader, "MOD_FILTROS_TERRITORIALES");

        // 2. Asegurar existencia de reportes
        if (repositorioReporteEmocion.contarTotal() == 0) {
            servicioAgregacionMapa.sembrarReportesIniciales();
        }

        List<Zona> zonasManuales = repositorioZona.obtenerZonasManuales();
        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();
        List<ZonaTerritorialAnaliticaDto> zonas = calcularZonasAnaliticas(zonasManuales, todosReportes);

        // 3. Filtrar según criterios especificados
        List<ZonaTerritorialAnaliticaDto> filtradas = zonas.stream()
                .filter(z -> {
                    if (zonaManualId != null && !zonaManualId.isBlank()) {
                        return z.getZonaManualId() != null && z.getZonaManualId().equalsIgnoreCase(zonaManualId);
                    }
                    return true;
                })
                .filter(z -> {
                    if (idCeldaH3 != null && !idCeldaH3.isBlank()) {
                        return z.getIdCeldaH3() != null && z.getIdCeldaH3().equalsIgnoreCase(idCeldaH3);
                    }
                    return true;
                })
                .filter(z -> {
                    if (emocion != null && !emocion.isBlank() && !"TODAS".equalsIgnoreCase(emocion)) {
                        return z.isCumpleUmbral() && z.getEmocionPredominante() != null
                                && z.getEmocionPredominante().equalsIgnoreCase(emocion);
                    }
                    return true;
                })
                .toList();

        long duracionMs = System.currentTimeMillis() - inicioMs;

        return FiltroTerritorialDashboardRespuesta.builder()
                .zonaFiltrada(zonaManualId != null ? zonaManualId : (idCeldaH3 != null ? idCeldaH3 : "TODAS"))
                .emocionFiltrada(emocion != null ? emocion : "TODAS")
                .periodoConsultado(periodo != null ? periodo : "ULTIMAS_24_HORAS")
                .totalZonasCoincidentes(filtradas.size())
                .zonasFiltradas(filtradas)
                .tiempoCalculoMs(duracionMs)
                .build();
    }

    /**
     * Exporta el reporte analítico territorial para toma de decisiones (MOD_EXPORTACION_REPORTES).
     * Exclusivo de suscripciones con PLAN_ENTERPRISE.
     */
    public ReporteAnaliticoExportacionRespuesta exportarReporteAnalitico(String authorizationHeader, String zonaManualId) {
        ResultadoValidacionModulo validacion = validadorPermisosBackend
                .validarAccesoModuloPorToken(authorizationHeader, "MOD_EXPORTACION_REPORTES");

        ClienteInstitucional cliente = repositorioClienteInstitucional.findById(validacion.getIdInstitucion())
                .orElseThrow(() -> new ExcepcionModuloRestringidoPorPlan(
                        "CLIENTE_NO_ENCONTRADO",
                        "No se encontró el registro del cliente institucional autenticado"
                ));

        if (repositorioReporteEmocion.contarTotal() == 0) {
            servicioAgregacionMapa.sembrarReportesIniciales();
        }

        List<Zona> zonasManuales = repositorioZona.obtenerZonasManuales();
        List<ReporteEmocion> todosReportes = repositorioReporteEmocion.obtenerTodos();
        List<ZonaTerritorialAnaliticaDto> zonas = calcularZonasAnaliticas(zonasManuales, todosReportes);

        if (zonaManualId != null && !zonaManualId.isBlank()) {
            zonas = zonas.stream()
                    .filter(z -> z.getZonaManualId() != null && z.getZonaManualId().equalsIgnoreCase(zonaManualId))
                    .toList();
        }

        return ReporteAnaliticoExportacionRespuesta.builder()
                .idInstitucion(cliente.getIdUsuario())
                .razonSocial(cliente.getRazonSocial())
                .identificacionFiscalNit(cliente.getIdentificacionFiscalNit())
                .planSuscripcion(validacion.getCodigoPlan())
                .formato("JSON")
                .fechaGeneracion(Instant.now())
                .totalZonasExportadas(zonas.size())
                .declaracionPrivacidad("Datos anonimizados agregados de conformidad con el principio de k-anonimato (k >= 5).")
                .datosZonas(zonas)
                .build();
    }

    private List<ZonaTerritorialAnaliticaDto> calcularZonasAnaliticas(List<Zona> zonasManuales, List<ReporteEmocion> todosReportes) {
        Map<String, List<ReporteEmocion>> reportesPorCelda = todosReportes.stream()
                .filter(r -> r.getIdCeldaH3() != null)
                .collect(Collectors.groupingBy(ReporteEmocion::getIdCeldaH3));

        Map<String, String> catalogoIconos = obtenerMapaIconosCatalogo();
        List<ZonaTerritorialAnaliticaDto> resultado = new ArrayList<>();

        for (Zona zona : zonasManuales) {
            String celdaH3 = zona.getIdCeldaH3();
            List<ReporteEmocion> reportesEnZona = reportesPorCelda.getOrDefault(celdaH3, List.of());
            int totalReportes = reportesEnZona.size();

            boolean cumpleUmbral = totalReportes >= UMBRAL_K_ANONIMATO;

            if (cumpleUmbral) {
                TipoEmocion emocionDominante = calcularEmocionPredominante(reportesEnZona);
                double intensidadProm = reportesEnZona.stream()
                        .mapToDouble(r -> r.getIntensidad() != null ? r.getIntensidad() : 3.0)
                        .average()
                        .orElse(3.0);
                intensidadProm = Math.round(intensidadProm * 10.0) / 10.0;

                String participantesAprox = servicioDetalleZona.calcularParticipantesAproximados(totalReportes);
                Map<String, Integer> distribucion = servicioDetalleZona.calcularDistribucionExacta(reportesEnZona);
                String tendencia = servicioDetalleZona.calcularTendencia(emocionDominante, reportesEnZona);

                resultado.add(ZonaTerritorialAnaliticaDto.builder()
                        .idCeldaH3(celdaH3)
                        .zonaManualId(zona.getZonaManualId())
                        .nombreZona(zona.getNombreZonaManual() != null ? zona.getNombreZonaManual() : "Zona Popayán")
                        .descripcionZona(zona.getDescripcion() != null ? zona.getDescripcion() : "Sector urbano monitoreado")
                        .latitudCentroide(zona.getLatitudCentroide() != null ? zona.getLatitudCentroide() : 2.4419)
                        .longitudCentroide(zona.getLongitudCentroide() != null ? zona.getLongitudCentroide() : -76.6063)
                        .cumpleUmbral(true)
                        .emocionPredominante(emocionDominante.name())
                        .nombreEmocionPredominante(emocionDominante.getEtiquetaVisible())
                        .codigoHexColor(emocionDominante.getCodigoHexColor())
                        .icono(resolverIcono(emocionDominante, catalogoIconos))
                        .intensidadPromedio(intensidadProm)
                        .participantesAproximados(participantesAprox)
                        .distribucionPorcentual(distribucion)
                        .tendencia(tendencia)
                        .build());
            } else {
                // k-anonimato: Ocultar emoción y distribución para evitar inferencia individual
                resultado.add(ZonaTerritorialAnaliticaDto.builder()
                        .idCeldaH3(celdaH3)
                        .zonaManualId(zona.getZonaManualId())
                        .nombreZona(zona.getNombreZonaManual() != null ? zona.getNombreZonaManual() : "Zona Popayán")
                        .descripcionZona(zona.getDescripcion() != null ? zona.getDescripcion() : "Sector urbano en reserva")
                        .latitudCentroide(zona.getLatitudCentroide() != null ? zona.getLatitudCentroide() : 2.4419)
                        .longitudCentroide(zona.getLongitudCentroide() != null ? zona.getLongitudCentroide() : -76.6063)
                        .cumpleUmbral(false)
                        .emocionPredominante(null)
                        .nombreEmocionPredominante(null)
                        .codigoHexColor("#94A3B8")
                        .icono("Lock")
                        .intensidadPromedio(0.0)
                        .participantesAproximados("En reserva de privacidad (< 5 vecinos)")
                        .distribucionPorcentual(Map.of())
                        .tendencia("Sin suficientes datos para inferir tendencia")
                        .build());
            }
        }

        return resultado;
    }

    private List<IndicadorEmocionCiudadDto> calcularDistribucionGlobalCiudad(List<ReporteEmocion> reportes) {
        Map<String, String> catalogoIconos = obtenerMapaIconosCatalogo();
        if (reportes == null || reportes.isEmpty()) {
            return Arrays.stream(TipoEmocion.values())
                    .map(em -> IndicadorEmocionCiudadDto.builder()
                            .idEmocion(em.name())
                            .nombreEmocion(em.getEtiquetaVisible())
                            .codigoHexColor(em.getCodigoHexColor())
                            .icono(resolverIcono(em, catalogoIconos))
                            .porcentajeGlobal(0)
                            .build())
                    .toList();
        }

        Map<String, Integer> distribucionExacta = servicioDetalleZona.calcularDistribucionExacta(reportes);

        List<IndicadorEmocionCiudadDto> resultado = new ArrayList<>();
        for (TipoEmocion em : TipoEmocion.values()) {
            int pct = distribucionExacta.getOrDefault(em.name(), 0);
            resultado.add(IndicadorEmocionCiudadDto.builder()
                    .idEmocion(em.name())
                    .nombreEmocion(em.getEtiquetaVisible())
                    .codigoHexColor(em.getCodigoHexColor())
                    .icono(resolverIcono(em, catalogoIconos))
                    .porcentajeGlobal(pct)
                    .build());
        }

        return resultado;
    }

    private TipoEmocion calcularEmocionPredominanteGlobal(List<ReporteEmocion> reportes) {
        if (reportes == null || reportes.isEmpty()) {
            return TipoEmocion.NEUTRALIDAD;
        }
        return calcularEmocionPredominante(reportes);
    }

    private double calcularIntensidadPromedioGlobal(List<ReporteEmocion> reportes) {
        if (reportes == null || reportes.isEmpty()) {
            return 0.0;
        }
        double avg = reportes.stream()
                .mapToDouble(r -> r.getIntensidad() != null ? r.getIntensidad() : 3.0)
                .average()
                .orElse(3.0);
        return Math.round(avg * 10.0) / 10.0;
    }

    private TipoEmocion calcularEmocionPredominante(List<ReporteEmocion> reportes) {
        Map<TipoEmocion, Long> conteo = reportes.stream()
                .filter(r -> r.getEmocion() != null)
                .collect(Collectors.groupingBy(ReporteEmocion::getEmocion, Collectors.counting()));

        return conteo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TipoEmocion.NEUTRALIDAD);
    }

    private String resolverIcono(TipoEmocion emocion, Map<String, String> catalogoIconos) {
        if (emocion == null) return "MapPin";
        if (catalogoIconos != null && catalogoIconos.containsKey(emocion.name())) {
            String icono = catalogoIconos.get(emocion.name());
            if (icono != null && !icono.isBlank()) {
                return icono;
            }
        }
        return switch (emocion) {
            case FELICIDAD -> "Smile";
            case NEUTRALIDAD -> "Meh";
            case PREOCUPACION -> "Frown";
            case ENFADO -> "Flame";
            case ANSIEDAD -> "CloudRain";
        };
    }
}
