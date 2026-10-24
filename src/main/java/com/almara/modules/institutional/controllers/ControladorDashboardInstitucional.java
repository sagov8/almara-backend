package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.DashboardInstitucionalResumenRespuesta;
import com.almara.modules.institutional.dto.FiltroTerritorialDashboardRespuesta;
import com.almara.modules.institutional.dto.ReporteAnaliticoExportacionRespuesta;
import com.almara.modules.institutional.services.ServicioDashboardInstitucional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el panel analítico del cliente institucional (HU-24, RF-34).
 * Expone endpoints para el resumen del clima emocional, filtrado territorial y exportación de reportes.
 */
@RestController
@RequestMapping("/api/v1/institutional/dashboard")
@CrossOrigin(origins = "*")
public class ControladorDashboardInstitucional {

    private final ServicioDashboardInstitucional servicioDashboardInstitucional;

    public ControladorDashboardInstitucional(ServicioDashboardInstitucional servicioDashboardInstitucional) {
        this.servicioDashboardInstitucional = servicioDashboardInstitucional;
    }

    /**
     * Consulta el resumen analítico general del clima emocional de Popayán (MOD_DASHBOARD_BASICO).
     */
    @GetMapping("/resumen")
    public ResponseEntity<DashboardInstitucionalResumenRespuesta> consultarDashboardResumen(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "periodo", defaultValue = "ULTIMAS_24_HORAS") String periodo) {
        DashboardInstitucionalResumenRespuesta resumen = servicioDashboardInstitucional
                .consultarDashboardResumen(authorizationHeader, periodo);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Filtra el clima emocional por zonas territoriales urbanas de Popayán (<<extend>> MOD_FILTROS_TERRITORIALES).
     */
    @GetMapping("/filtrar-zonas")
    public ResponseEntity<FiltroTerritorialDashboardRespuesta> filtrarClimaEmocionalPorZonas(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "zonaManualId", required = false) String zonaManualId,
            @RequestParam(value = "idCeldaH3", required = false) String idCeldaH3,
            @RequestParam(value = "emocion", required = false) String emocion,
            @RequestParam(value = "periodo", defaultValue = "ULTIMAS_24_HORAS") String periodo) {
        FiltroTerritorialDashboardRespuesta respuesta = servicioDashboardInstitucional
                .filtrarClimaEmocionalPorZonas(authorizationHeader, zonaManualId, idCeldaH3, emocion, periodo);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Exporta el reporte analítico territorial para planes empresariales (MOD_EXPORTACION_REPORTES).
     */
    @GetMapping("/exportar")
    public ResponseEntity<ReporteAnaliticoExportacionRespuesta> exportarReporteAnalitico(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(value = "zonaManualId", required = false) String zonaManualId) {
        ReporteAnaliticoExportacionRespuesta exportacion = servicioDashboardInstitucional
                .exportarReporteAnalitico(authorizationHeader, zonaManualId);
        return ResponseEntity.ok(exportacion);
    }
}
