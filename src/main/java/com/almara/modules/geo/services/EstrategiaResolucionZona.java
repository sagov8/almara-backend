package com.almara.modules.geo.services;

import com.almara.modules.geo.models.ResolucionZonaContexto;
import com.almara.modules.geo.models.ResultadoResolucionZona;

/**
 * Interfaz del patrón Strategy para la resolución de celdas geográficas H3 (HU-04).
 * Permite intercambiar de forma transparente la resolución por coordenadas GPS temporales
 * o por catálogo manual de sectores predefinidos.
 */
public interface EstrategiaResolucionZona {

    /**
     * Evalúa si esta estrategia es aplicable para el contexto dado.
     */
    boolean aplica(ResolucionZonaContexto contexto);

    /**
     * Ejecuta la resolución de la zona y genera el resultado con la celda H3.
     */
    ResultadoResolucionZona resolver(ResolucionZonaContexto contexto);
}
