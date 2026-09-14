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

/**
 * Controlador REST para la selección y registro de emociones ciudadanas (HU-01 y HU-04).
 */
@RestController
@RequestMapping("/api/v1/emociones")
@CrossOrigin(origins = "*") // Permite llamadas desde Expo / React Native
public class ControladorEmocion {

    private final ServicioRegistroEmocion servicioRegistroEmocion;

    public ControladorEmocion(ServicioRegistroEmocion servicioRegistroEmocion) {
        this.servicioRegistroEmocion = servicioRegistroEmocion;
    }

    /**
     * Endpoint principal para seleccionar y registrar una emoción anónima asociada a una celda H3.
     * Tiempo de respuesta garantizado < 1000 ms bajo condiciones normales.
     */
    @PostMapping("/seleccionar")
    public ResponseEntity<RegistroEmocionRespuesta> seleccionarEmocion(
            @Valid @RequestBody RegistroEmocionSolicitud solicitud) {

        RegistroEmocionRespuesta respuesta = servicioRegistroEmocion.registrarEmocion(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /**
     * Endpoint para consultar las opciones del catálogo predeterminado de emociones.
     */
    @GetMapping("/catalogo")
    public ResponseEntity<List<ElementoCatalogoEmocion>> obtenerCatalogo() {
        List<ElementoCatalogoEmocion> catalogo = Arrays.stream(TipoEmocion.values())
                .map(e -> ElementoCatalogoEmocion.builder()
                        .id(e)
                        .etiquetaVisible(e.getEtiquetaVisible())
                        .codigoHexColor(e.getCodigoHexColor())
                        .codigoHexFondo(e.getCodigoHexFondo())
                        .build())
                .toList();

        return ResponseEntity.ok(catalogo);
    }
}
