package com.almara.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador centralizado de excepciones para la API REST de Almara.
 * Captura solicitudes inválidas, violaciones de catálogo predeterminado y excesos de frecuencia.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ExcepcionSolicitudesExcesivas.class)
    public ResponseEntity<RespuestaError> manejarSolicitudesExcesivas(
            ExcepcionSolicitudesExcesivas ex,
            HttpServletRequest peticion) {

        Map<String, Object> detalles = new HashMap<>();
        detalles.put("segundosRestantes", ex.getSegundosRestantes());

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("SOLICITUDES_EXCESIVAS")
                .mensaje(ex.getMessage())
                .ruta(peticion.getRequestURI())
                .detallesAdicionales(detalles)
                .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacionArgumentos(
            MethodArgumentNotValidException ex,
            HttpServletRequest peticion) {

        Map<String, Object> camposConError = new HashMap<>();
        for (FieldError errorCampo : ex.getBindingResult().getFieldErrors()) {
            camposConError.put(errorCampo.getField(), errorCampo.getDefaultMessage());
        }

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.BAD_REQUEST.value())
                .error("VALIDACION_FALLIDA")
                .mensaje("Los datos enviados no cumplen con el formato o restricciones requeridas")
                .ruta(peticion.getRequestURI())
                .detallesAdicionales(camposConError)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarMensajeNoLegible(
            HttpMessageNotReadableException ex,
            HttpServletRequest peticion) {

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.BAD_REQUEST.value())
                .error("CUERPO_PETICION_INVALIDO")
                .mensaje("La emoción enviada no pertenece al catálogo predeterminado o el formato JSON es incorrecto")
                .ruta(peticion.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExcepcionUbicacionRequerida.class)
    public ResponseEntity<RespuestaError> manejarUbicacionRequerida(
            ExcepcionUbicacionRequerida ex,
            HttpServletRequest peticion) {

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.BAD_REQUEST.value())
                .error("UBICACION_REQUERIDA")
                .mensaje(ex.getMessage())
                .ruta(peticion.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExcepcionZonaNoEncontrada.class)
    public ResponseEntity<RespuestaError> manejarZonaNoEncontrada(
            ExcepcionZonaNoEncontrada ex,
            HttpServletRequest peticion) {

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.NOT_FOUND.value())
                .error("ZONA_NO_ENCONTRADA")
                .mensaje(ex.getMessage())
                .ruta(peticion.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarExcepcionGeneral(
            Exception ex,
            HttpServletRequest peticion) {

        RespuestaError error = RespuestaError.builder()
                .marcaTemporal(Instant.now())
                .codigoEstado(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("ERROR_INTERNO_SERVIDOR")
                .mensaje("Ocurrió un error inesperado al procesar la solicitud: " + ex.getMessage())
                .ruta(peticion.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
