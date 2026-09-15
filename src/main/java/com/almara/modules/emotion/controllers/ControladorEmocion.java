package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.ElementoCatalogoEmocion;
import com.almara.modules.emotion.models.RegistroEmocionRespuesta;
import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.services.ServicioRegistroEmocion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import com.almara.modules.emotion.models.RegistroIntensidadRespuesta;
import com.almara.modules.emotion.models.RegistroIntensidadSolicitud;
import com.almara.modules.emotion.services.ServicioRegistroIntensidad;
import java.util.UUID;

import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;

/**
 * Controlador REST para la selección y registro de emociones ciudadanas (HU-01, HU-02 y HU-04).
 */
@RestController
@RequestMapping("/api/v1/emociones")
@CrossOrigin(origins = "*") // Permite llamadas desde Expo / React Native
public class ControladorEmocion {

    private final ServicioRegistroEmocion servicioRegistroEmocion;
    private final ServicioRegistroIntensidad servicioRegistroIntensidad;
    private final RepositorioCatalogoEmocion repositorioCatalogoEmocion;

    public ControladorEmocion(
            ServicioRegistroEmocion servicioRegistroEmocion,
            ServicioRegistroIntensidad servicioRegistroIntensidad,
            RepositorioCatalogoEmocion repositorioCatalogoEmocion) {
        this.servicioRegistroEmocion = servicioRegistroEmocion;
        this.servicioRegistroIntensidad = servicioRegistroIntensidad;
        this.repositorioCatalogoEmocion = repositorioCatalogoEmocion;
    }

    /**
     * Endpoint principal para seleccionar y registrar una emoción anónima asociada a una celda H3 (HU-01).
     * Tiempo de respuesta garantizado < 1000 ms bajo condiciones normales.
     */
    @PostMapping("/seleccionar")
    public ResponseEntity<RegistroEmocionRespuesta> seleccionarEmocion(
            @Valid @RequestBody RegistroEmocionSolicitud solicitud) {

        RegistroEmocionRespuesta respuesta = servicioRegistroEmocion.registrarEmocion(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Endpoint para indicar y registrar la intensidad de una emoción previamente seleccionada (HU-02).
     * Tiempo de respuesta garantizado < 300 ms bajo condiciones normales (RNF Desempeño).
     */
    @PostMapping("/intensidad")
    public ResponseEntity<RegistroIntensidadRespuesta> registrarIntensidad(
            @Valid @RequestBody RegistroIntensidadSolicitud solicitud) {

        RegistroIntensidadRespuesta respuesta = servicioRegistroIntensidad.registrarIntensidad(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint semántico REST alternativo para registrar la intensidad mediante ruta identificadora (HU-02).
     */
    @PatchMapping("/{idEvento}/intensidad")
    public ResponseEntity<RegistroIntensidadRespuesta> registrarIntensidadPorRuta(
            @PathVariable UUID idEvento,
            @Valid @RequestBody RegistroIntensidadSolicitud solicitud) {

        solicitud.setIdEvento(idEvento);
        RegistroIntensidadRespuesta respuesta = servicioRegistroIntensidad.registrarIntensidad(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint para consultar las opciones del catálogo predeterminado de emociones desde la base de datos (HU-12).
     */
    @GetMapping("/catalogo")
    public ResponseEntity<List<ElementoCatalogoEmocion>> obtenerCatalogo() {
        List<EntidadCatalogoEmocion> emocionesDb = repositorioCatalogoEmocion.findByActivoTrue();
        if (emocionesDb != null && !emocionesDb.isEmpty()) {
            List<ElementoCatalogoEmocion> catalogo = emocionesDb.stream()
                    .map(e -> ElementoCatalogoEmocion.builder()
                            .id(TipoEmocion.valueOf(e.getIdEmocion()))
                            .etiquetaVisible(e.getNombreEtiqueta())
                            .codigoHexColor(e.getCodigoHexColor())
                            .codigoHexFondo(e.getCodigoHexFondo())
                            .iconoSvg(e.getIconoSvg())
                            .descripcion(e.getDescripcion())
                            .build())
                    .toList();
            return ResponseEntity.ok(catalogo);
        }

        // Fallback al enum en caso de catálogo no sembrado
        List<ElementoCatalogoEmocion> catalogoFallback = Arrays.stream(TipoEmocion.values())
                .map(e -> ElementoCatalogoEmocion.builder()
                        .id(e)
                        .etiquetaVisible(e.getEtiquetaVisible())
                        .codigoHexColor(e.getCodigoHexColor())
                        .codigoHexFondo(e.getCodigoHexFondo())
                        .iconoSvg(obtenerIconoFallback(e))
                        .descripcion(e.getEtiquetaVisible())
                        .build())
                .toList();

        return ResponseEntity.ok(catalogoFallback);
    }

    private String obtenerIconoFallback(TipoEmocion e) {
        return switch (e) {
            case FELICIDAD -> "😊";
            case NEUTRALIDAD -> "😐";
            case PREOCUPACION -> "😟";
            case ENFADO -> "😠";
            case ANSIEDAD -> "😰";
        };
    }
}
