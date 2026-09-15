package com.almara.modules.aggregation.controllers;

import com.almara.modules.aggregation.models.CeldaMapaEmocional;
import com.almara.modules.aggregation.models.ConsultaMapaEmocionalRespuesta;
import com.almara.modules.aggregation.models.ContextoZoomMapa;
import com.almara.modules.aggregation.models.DetalleZonaEmocionRespuesta;
import com.almara.modules.aggregation.models.PeriodoTemporal;
import com.almara.modules.aggregation.services.ServicioAgregacionMapa;
import com.almara.modules.aggregation.services.ServicioDetalleZona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la consulta de la capa emocional del mapa interactivo (HU-05 y HU-06).
 * Implementa el patrón Strategy para la resolución H3 dinámica y detalle emocional por zona.
 */
@RestController
@RequestMapping("/api/v1/mapa")
@CrossOrigin(origins = "*")
public class ControladorMapa {

    private static final Logger log = LoggerFactory.getLogger(ControladorMapa.class);

    private final ServicioAgregacionMapa servicioAgregacionMapa;
    private final ServicioDetalleZona servicioDetalleZona;

    public ControladorMapa(ServicioAgregacionMapa servicioAgregacionMapa,
                           ServicioDetalleZona servicioDetalleZona) {
        this.servicioAgregacionMapa = servicioAgregacionMapa;
        this.servicioDetalleZona = servicioDetalleZona;
    }

    /**
     * Consulta celdas visibles que cumplen con el umbral de Protección en Comunidad (HU-05).
     */
    @GetMapping("/celdas")
    public ResponseEntity<ConsultaMapaEmocionalRespuesta> consultarCeldasVisibles(
            @RequestParam(name = "zoom", defaultValue = "14") int zoom,
            @RequestParam(name = "umbralK", defaultValue = "5") int umbralK,
            @RequestParam(name = "minLat", required = false) Double minLat,
            @RequestParam(name = "maxLat", required = false) Double maxLat,
            @RequestParam(name = "minLon", required = false) Double minLon,
            @RequestParam(name = "maxLon", required = false) Double maxLon
    ) {
        log.info("GET /api/v1/mapa/celdas -> zoom={}, umbralK={}, bbox=[{}, {}, {}, {}]",
                zoom, umbralK, minLat, maxLat, minLon, maxLon);

        ContextoZoomMapa contexto = ContextoZoomMapa.builder()
                .nivelZoom(zoom)
                .umbralMinimoK(umbralK)
                .minLat(minLat)
                .maxLat(maxLat)
                .minLon(minLon)
                .maxLon(maxLon)
                .build();

        ConsultaMapaEmocionalRespuesta respuesta = servicioAgregacionMapa.consultarCeldas(contexto);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint directo optimizado para el centro urbano de Popayán.
     */
    @GetMapping("/popayan")
    public ResponseEntity<ConsultaMapaEmocionalRespuesta> consultarMapaPopayan(
            @RequestParam(name = "zoom", defaultValue = "14") int zoom,
            @RequestParam(name = "umbralK", defaultValue = "5") int umbralK
    ) {
        // Coordenadas aproximadas del cuadrante urbano de Popayán
        ContextoZoomMapa contexto = ContextoZoomMapa.builder()
                .nivelZoom(zoom)
                .umbralMinimoK(umbralK)
                .minLat(2.4000)
                .maxLat(2.5100)
                .minLon(-76.6500)
                .maxLon(-76.5400)
                .build();

        ConsultaMapaEmocionalRespuesta respuesta = servicioAgregacionMapa.consultarCeldas(contexto);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Consulta todas las celdas (visibles y protegidas en reserva) para inspección de privacidad.
     */
    @GetMapping("/todas-celdas")
    public ResponseEntity<List<CeldaMapaEmocional>> consultarTodasLasCeldas(
            @RequestParam(name = "zoom", defaultValue = "14") int zoom,
            @RequestParam(name = "umbralK", defaultValue = "5") int umbralK
    ) {
        ContextoZoomMapa contexto = ContextoZoomMapa.builder()
                .nivelZoom(zoom)
                .umbralMinimoK(umbralK)
                .build();

        List<CeldaMapaEmocional> todas = servicioAgregacionMapa.obtenerTodasLasCeldas(contexto);
        return ResponseEntity.ok(todas);
    }

    /**
     * Consulta la distribución emocional detallada de una celda seleccionada (HU-06).
     */
    @GetMapping("/zona/{idCeldaH3}/detalle")
    public ResponseEntity<DetalleZonaEmocionRespuesta> consultarDetalleZona(
            @PathVariable(name = "idCeldaH3") String idCeldaH3,
            @RequestParam(name = "periodo", defaultValue = "ULTIMAS_2_HORAS") String periodo
    ) {
        log.info("GET /api/v1/mapa/zona/{}/detalle?periodo={}", idCeldaH3, periodo);

        PeriodoTemporal periodoTemporal = PeriodoTemporal.desdeCadena(periodo);
        DetalleZonaEmocionRespuesta detalle = servicioDetalleZona.consultarDetalleZona(idCeldaH3, periodoTemporal);

        return ResponseEntity.ok(detalle);
    }
}
