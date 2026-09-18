package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.ElementoCatalogoEmocion;
import com.almara.modules.emotion.models.RegistroEmocionRespuesta;
import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.services.ServicioRegistroEmocion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.almara.modules.emotion.models.RegistroIntensidadRespuesta;
import com.almara.modules.emotion.models.RegistroIntensidadSolicitud;
import com.almara.modules.emotion.services.ServicioRegistroIntensidad;
import java.util.UUID;

import com.almara.modules.emotion.models.RegistroComentarioRespuesta;
import com.almara.modules.emotion.models.RegistroComentarioSolicitud;
import com.almara.modules.emotion.services.ServicioCatalogoEmocion;
import com.almara.modules.emotion.services.ServicioRegistroComentario;

/**
 * Controlador REST para la selección y registro de emociones ciudadanas (HU-01, HU-02, HU-03, HU-04 y HU-12).
 */
@RestController
@RequestMapping("/api/v1/emociones")
@CrossOrigin(origins = "*") // Permite llamadas desde Expo / React Native
public class ControladorEmocion {

    private final ServicioRegistroEmocion servicioRegistroEmocion;
    private final ServicioRegistroIntensidad servicioRegistroIntensidad;
    private final ServicioRegistroComentario servicioRegistroComentario;
    private final ServicioCatalogoEmocion servicioCatalogoEmocion;

    public ControladorEmocion(
            ServicioRegistroEmocion servicioRegistroEmocion,
            ServicioRegistroIntensidad servicioRegistroIntensidad,
            ServicioRegistroComentario servicioRegistroComentario,
            ServicioCatalogoEmocion servicioCatalogoEmocion) {
        this.servicioRegistroEmocion = servicioRegistroEmocion;
        this.servicioRegistroIntensidad = servicioRegistroIntensidad;
        this.servicioRegistroComentario = servicioRegistroComentario;
        this.servicioCatalogoEmocion = servicioCatalogoEmocion;
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
     * Endpoint para ingresar un comentario complementario y opcional a la emoción (HU-03).
     * Tiempo de respuesta garantizado < 300 ms bajo condiciones normales (RNF Desempeño).
     */
    @PostMapping("/comentario")
    public ResponseEntity<RegistroComentarioRespuesta> registrarComentario(
            @Valid @RequestBody RegistroComentarioSolicitud solicitud) {

        RegistroComentarioRespuesta respuesta = servicioRegistroComentario.registrarComentario(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint semántico REST alternativo para registrar comentario complementario por ID de ruta (HU-03).
     */
    @PatchMapping("/{idEvento}/comentario")
    public ResponseEntity<RegistroComentarioRespuesta> registrarComentarioPorRuta(
            @PathVariable UUID idEvento,
            @Valid @RequestBody RegistroComentarioSolicitud solicitud) {

        solicitud.setIdEvento(idEvento);
        RegistroComentarioRespuesta respuesta = servicioRegistroComentario.registrarComentario(solicitud);
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint para consultar las opciones del catálogo predeterminado de emociones desde la base de datos (HU-12).
     */
    @GetMapping("/catalogo")
    public ResponseEntity<List<ElementoCatalogoEmocion>> obtenerCatalogo() {
        return ResponseEntity.ok(servicioCatalogoEmocion.obtenerCatalogo());
    }
}
