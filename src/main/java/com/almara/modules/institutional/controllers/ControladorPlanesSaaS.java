package com.almara.modules.institutional.controllers;

import com.almara.modules.institutional.dto.CatalogoPlanesRespuesta;
import com.almara.modules.institutional.services.ServicioCatalogoPlanesSaaS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para el Catálogo de Servicios y Planes SaaS Institucionales (HU-19).
 */
@RestController
@RequestMapping("/api/v1/institutional/planes")
@CrossOrigin(origins = "*")
public class ControladorPlanesSaaS {

    private static final Logger log = LoggerFactory.getLogger(ControladorPlanesSaaS.class);

    private final ServicioCatalogoPlanesSaaS servicioCatalogoPlanesSaaS;

    public ControladorPlanesSaaS(ServicioCatalogoPlanesSaaS servicioCatalogoPlanesSaaS) {
        this.servicioCatalogoPlanesSaaS = servicioCatalogoPlanesSaaS;
    }

    /**
     * Consulta el catálogo comercial de planes SaaS activos con desglose de módulos (HU-19).
     */
    @GetMapping("/catalogo")
    public ResponseEntity<CatalogoPlanesRespuesta> consultarCatalogo(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("GET /api/v1/institutional/planes/catalogo");

        CatalogoPlanesRespuesta respuesta = servicioCatalogoPlanesSaaS.consultarCatalogoPlanes(authorization);
        return ResponseEntity.ok(respuesta);
    }
}
