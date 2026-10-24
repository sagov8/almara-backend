package com.almara.modules.emotion.services;

import com.almara.modules.emotion.models.ElementoCatalogoEmocion;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Servicio de aplicación para la consulta y provisión del catálogo de emociones (HU-12).
 * Gestiona la recuperación de emociones activas desde la base de datos y provee
 * un mecanismo de respaldo (fallback) basado en el enum TipoEmocion en caso de ausencia de datos sembrados.
 */
@Service
public class ServicioCatalogoEmocion {

    private static final Logger log = LoggerFactory.getLogger(ServicioCatalogoEmocion.class);

    private final RepositorioCatalogoEmocion repositorioCatalogoEmocion;

    public ServicioCatalogoEmocion(RepositorioCatalogoEmocion repositorioCatalogoEmocion) {
        this.repositorioCatalogoEmocion = repositorioCatalogoEmocion;
    }

    /**
     * Obtiene el catálogo predeterminado de emociones activas.
     * Si existen registros en base de datos, los transforma a DTOs.
     * Si no existen registros activos o la tabla no está sembrada, utiliza el catálogo de respaldo (fallback).
     *
     * @return Lista de ElementoCatalogoEmocion disponibles.
     */
    public List<ElementoCatalogoEmocion> obtenerCatalogo() {
        List<EntidadCatalogoEmocion> emocionesDb = repositorioCatalogoEmocion.findByActivoTrue();
        if (emocionesDb != null && !emocionesDb.isEmpty()) {
            return emocionesDb.stream()
                    .map(e -> ElementoCatalogoEmocion.builder()
                            .id(resolverTipoEmocionSeguro(e.getIdEmocion()))
                            .idEmocion(e.getIdEmocion())
                            .etiquetaVisible(e.getNombreEtiqueta())
                            .codigoHexColor(e.getCodigoHexColor())
                            .codigoHexFondo(e.getCodigoHexFondo())
                            .iconoSvg(e.getIconoSvg())
                            .descripcion(e.getDescripcion())
                            .build())
                    .toList();
        }

        log.warn("Catálogo de emociones no encontrado o vacío en la base de datos. Utilizando fallback basado en TipoEmocion.");
        return obtenerCatalogoFallback();
    }

    /**
     * Genera el catálogo de respaldo a partir del enum TipoEmocion cuando no hay datos sembrados.
     */
    public List<ElementoCatalogoEmocion> obtenerCatalogoFallback() {
        return Arrays.stream(TipoEmocion.values())
                .map(e -> ElementoCatalogoEmocion.builder()
                        .id(e)
                        .etiquetaVisible(e.getEtiquetaVisible())
                        .codigoHexColor(e.getCodigoHexColor())
                        .codigoHexFondo(e.getCodigoHexFondo())
                        .iconoSvg(obtenerIconoFallback(e))
                        .descripcion(e.getEtiquetaVisible())
                        .build())
                .toList();
    }

    /**
     * Retorna el icono emoji predeterminado para cada emoción en caso de fallback.
     */
    private String obtenerIconoFallback(TipoEmocion e) {
        return switch (e) {
            case FELICIDAD -> "😊";
            case NEUTRALIDAD -> "😐";
            case PREOCUPACION -> "😟";
            case ENFADO -> "😠";
            case ANSIEDAD -> "😰";
        };
    }

    private TipoEmocion resolverTipoEmocionSeguro(String idEmocion) {
        if (idEmocion == null) {
            return null;
        }
        try {
            return TipoEmocion.valueOf(idEmocion.trim().toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }
}
