package com.almara.modules.geo.services;

import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.ResolucionZonaContexto;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Estrategia de resolución automática de celda H3 a partir de coordenadas GPS temporales (HU-04).
 * Cumple con RNF Seguridad y Privacy by Design:
 * Las coordenadas exactas no se guardan ni se registran en los logs del servidor;
 * únicamente se usan en el marco de ejecución de este método para calcular el índice H3.
 */
@Component
@Order(1)
public class GpsToH3ResolutionStrategy implements EstrategiaResolucionZona {

    private static final Logger log = LoggerFactory.getLogger(GpsToH3ResolutionStrategy.class);

    private final AdaptadorH3 adaptadorH3;

    public GpsToH3ResolutionStrategy(AdaptadorH3 adaptadorH3) {
        this.adaptadorH3 = adaptadorH3;
    }

    @Override
    public boolean aplica(ResolucionZonaContexto contexto) {
        return contexto != null && contexto.tieneCoordenadasGps();
    }

    @Override
    public ResultadoResolucionZona resolver(ResolucionZonaContexto contexto) {
        CoordenadasGps coords = contexto.getCoordenadasGps();
        int resolucion = contexto.getResolucionDeseada();

        // Conversión espacial en memoria
        String idCeldaH3 = adaptadorH3.coordenadasACelda(coords.getLatitud(), coords.getLongitud(), resolucion);
        CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCeldaH3);

        // Registro de auditoría anónimo: solo la celda H3 y resolución, sin coordenadas precisas
        log.info("Celda H3 resuelta por GPS con éxito: [Celda: {}, Resolución: {}]", idCeldaH3, resolucion);

        return ResultadoResolucionZona.builder()
                .idCeldaH3(idCeldaH3)
                .resolucionH3(resolucion)
                .nombreZona("Celda H3 " + idCeldaH3)
                .esManual(false)
                .latitudCentroide(centroide.getLatitud())
                .longitudCentroide(centroide.getLongitud())
                .build();
    }
}
