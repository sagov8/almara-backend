package com.almara.modules.emotion.services;

import com.almara.modules.emotion.models.EntidadReporteEmocion;
import com.almara.modules.emotion.repositories.RepositorioReporteEmocionJpa;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicioHistoricoEmociones {

    private final RepositorioReporteEmocionJpa repositorioReporteEmocion;

    public ServicioHistoricoEmociones (RepositorioReporteEmocionJpa repositorioReporteEmocion) {
        this.repositorioReporteEmocion = repositorioReporteEmocion;
    }

    public List<EntidadReporteEmocion> mostrarHistoricoEmociones() {
        return repositorioReporteEmocion.findAll();
    }
}
