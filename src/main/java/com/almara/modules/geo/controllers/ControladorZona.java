package com.almara.modules.geo.controllers;

import com.almara.modules.geo.models.ElementoCatalogoZona;
import com.almara.modules.geo.models.ResolucionZonaSolicitud;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import com.almara.modules.geo.services.ServicioResolucionZona;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones del módulo geoespacial (HU-04).
 * Expone la consulta del catálogo de zonas manuales y la resolución de celdas H3.
 */
@RestController
@RequestMapping("/api/v1/geo")
@CrossOrigin(origins = "*")
public class ControladorZona {

    private final ServicioResolucionZona servicioResolucionZona;

    public ControladorZona(ServicioResolucionZona servicioResolucionZona) {
        this.servicioResolucionZona = servicioResolucionZona;
    }

    /**
     * Endpoint para consultar el catálogo de sectores predefinidos para selección manual.
     * Utilizado por la interfaz ciudadana cuando el usuario rechaza permisos GPS.
     */
    @GetMapping("/zonas-manuales")
    public ResponseEntity<List<ElementoCatalogoZona>> obtenerZonasManuales() {
        List<ElementoCatalogoZona> catalogo = servicioResolucionZona.obtenerCatalogoZonasManuales();
        return ResponseEntity.ok(catalogo);
    }

    /**
     * Endpoint para resolver una celda H3 a partir de coordenadas GPS efímeras
     * o por código de zona manual.
     */
    @PostMapping("/resolver-celda")
    public ResponseEntity<ResultadoResolucionZona> resolverCelda(
            @Valid @RequestBody ResolucionZonaSolicitud solicitud) {

        ResultadoResolucionZona resultado = servicioResolucionZona.resolverZona(
                solicitud.getCoordenadasGps(),
                solicitud.getZonaManualId()
        );

        return ResponseEntity.ok(resultado);
    }
}
