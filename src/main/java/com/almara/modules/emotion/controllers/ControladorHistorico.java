package com.almara.modules.emotion.controllers;

import com.almara.modules.emotion.models.ElementoCatalogoEmocion;
import com.almara.modules.emotion.models.EntidadReporteEmocion;
import com.almara.modules.emotion.services.ServicioHistoricoEmociones;
import com.almara.modules.emotion.services.ServicioRegistroEmocion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emociones")
@CrossOrigin(origins = "*")
public class ControladorHistorico {

    private final ServicioHistoricoEmociones servicioHistoricoEmociones;

    public ControladorHistorico(ServicioHistoricoEmociones servicioHistoricoEmociones, ServicioRegistroEmocion servicioRegistroEmocion) {
        this.servicioHistoricoEmociones = servicioHistoricoEmociones;
    }

    @GetMapping("/historico")
    public ResponseEntity<List<EntidadReporteEmocion>> obtenerHistorico() {
        return ResponseEntity.ok(servicioHistoricoEmociones.mostrarHistoricoEmociones());
    }

}
