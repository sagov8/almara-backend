package com.almara.modules.emotion.services;

import com.almara.modules.emotion.models.ElementoCatalogoEmocion;
import com.almara.modules.emotion.models.EntidadCatalogoEmocion;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.repositories.RepositorioCatalogoEmocion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicioCatalogoEmocionPruebaTest {

    @Mock
    private RepositorioCatalogoEmocion repositorioCatalogoEmocion;

    @InjectMocks
    private ServicioCatalogoEmocion servicioCatalogoEmocion;

    private EntidadCatalogoEmocion entidadEjemplo;

    @BeforeEach
    void setUp() {
        entidadEjemplo = EntidadCatalogoEmocion.builder()
                .idEmocion("FELICIDAD")
                .nombreEtiqueta("Felicidad")
                .codigoHexColor("#FFD700")
                .codigoHexFondo("#FFF8DC")
                .iconoSvg("<svg>happy</svg>")
                .descripcion("Sensación de bienestar y alegría")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("HU-12: Retorna catálogo desde la base de datos cuando existen registros activos")
    void obtenerCatalogo_ConDatosEnBaseDeDatos_RetornaMapeado() {
        when(repositorioCatalogoEmocion.findByActivoTrue()).thenReturn(List.of(entidadEjemplo));

        List<ElementoCatalogoEmocion> resultado = servicioCatalogoEmocion.obtenerCatalogo();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        ElementoCatalogoEmocion elemento = resultado.get(0);
        assertEquals(TipoEmocion.FELICIDAD, elemento.getId());
        assertEquals("Felicidad", elemento.getEtiquetaVisible());
        assertEquals("#FFD700", elemento.getCodigoHexColor());
        assertEquals("#FFF8DC", elemento.getCodigoHexFondo());
        assertEquals("<svg>happy</svg>", elemento.getIconoSvg());
        assertEquals("Sensación de bienestar y alegría", elemento.getDescripcion());
        verify(repositorioCatalogoEmocion).findByActivoTrue();
    }

    @Test
    @DisplayName("HU-12: Utiliza fallback de TipoEmocion cuando la base de datos está vacía")
    void obtenerCatalogo_SinDatosEnBaseDeDatos_UsaFallback() {
        when(repositorioCatalogoEmocion.findByActivoTrue()).thenReturn(Collections.emptyList());

        List<ElementoCatalogoEmocion> resultado = servicioCatalogoEmocion.obtenerCatalogo();

        assertNotNull(resultado);
        assertEquals(TipoEmocion.values().length, resultado.size());
        assertTrue(resultado.stream().anyMatch(e -> e.getId() == TipoEmocion.FELICIDAD && "Smile".equals(e.getIconoSvg())));
        assertTrue(resultado.stream().anyMatch(e -> e.getId() == TipoEmocion.NEUTRALIDAD && "Meh".equals(e.getIconoSvg())));
        assertTrue(resultado.stream().anyMatch(e -> e.getId() == TipoEmocion.PREOCUPACION && "Frown".equals(e.getIconoSvg())));
        assertTrue(resultado.stream().anyMatch(e -> e.getId() == TipoEmocion.ENFADO && "Flame".equals(e.getIconoSvg())));
        assertTrue(resultado.stream().anyMatch(e -> e.getId() == TipoEmocion.ANSIEDAD && "CloudRain".equals(e.getIconoSvg())));
        verify(repositorioCatalogoEmocion).findByActivoTrue();
    }

    @Test
    @DisplayName("HU-12: Utiliza fallback cuando repositorio retorna null")
    void obtenerCatalogo_RepositorioRetornaNull_UsaFallback() {
        when(repositorioCatalogoEmocion.findByActivoTrue()).thenReturn(null);

        List<ElementoCatalogoEmocion> resultado = servicioCatalogoEmocion.obtenerCatalogo();

        assertNotNull(resultado);
        assertEquals(TipoEmocion.values().length, resultado.size());
    }

    @Test
    @DisplayName("HU-12: Fallback incluye todas las emociones predeterminadas")
    void obtenerCatalogoFallback_RetornaTodasLasEmociones() {
        List<ElementoCatalogoEmocion> fallback = servicioCatalogoEmocion.obtenerCatalogoFallback();

        assertNotNull(fallback);
        assertEquals(5, fallback.size());
        assertEquals(5, TipoEmocion.values().length);
    }
}
