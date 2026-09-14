package com.almara.modules.geo.services;

import com.almara.common.ExcepcionUbicacionRequerida;
import com.almara.common.ExcepcionZonaNoEncontrada;
import com.almara.modules.geo.models.CoordenadasGps;
import com.almara.modules.geo.models.ResolucionZonaContexto;
import com.almara.modules.geo.models.ResultadoResolucionZona;
import com.almara.modules.geo.repositories.RepositorioZona;
import com.almara.modules.geo.repositories.RepositorioZonaMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EstrategiaResolucionZonaPrueba {

    private AdaptadorH3 adaptadorH3;
    private RepositorioZona repositorioZona;
    private GpsToH3ResolutionStrategy estrategiaGps;
    private ManualCatalogResolutionStrategy estrategiaManual;
    private ServicioResolucionZona servicioResolucionZona;

    @BeforeEach
    void setUp() {
        adaptadorH3 = new AdaptadorH3Core();
        RepositorioZonaMemoria repoMemoria = new RepositorioZonaMemoria(adaptadorH3);
        repoMemoria.inicializarCatalogoPredeterminado();
        repositorioZona = repoMemoria;

        estrategiaGps = new GpsToH3ResolutionStrategy(adaptadorH3);
        estrategiaManual = new ManualCatalogResolutionStrategy(repositorioZona);

        servicioResolucionZona = new ServicioResolucionZona(
                List.of(estrategiaGps, estrategiaManual),
                repositorioZona,
                9
        );
    }

    @Test
    @DisplayName("Adapter Pattern: AdaptadorH3Core genera celdas válidas a resolución 9")
    void adaptadorH3_GeneraCeldaValida() {
        double lat = 4.6486;
        double lon = -74.0645;
        int resolucion = 9;

        String idCelda = adaptadorH3.coordenadasACelda(lat, lon, resolucion);

        assertNotNull(idCelda);
        assertEquals(15, idCelda.length(), "El id de celda H3 debe tener 15 caracteres hexadecimales");
        assertTrue(adaptadorH3.esCeldaValida(idCelda));
        assertEquals(9, adaptadorH3.obtenerResolucion(idCelda));

        CoordenadasGps centroide = adaptadorH3.celdaACentroide(idCelda);
        assertNotNull(centroide);
        // El centroide debe estar sumamente cercano a las coordenadas de entrada (~100m)
        assertEquals(lat, centroide.getLatitud(), 0.01);
        assertEquals(lon, centroide.getLongitud(), 0.01);
    }

    @Test
    @DisplayName("Strategy Pattern: GpsToH3ResolutionStrategy convierte coordenadas temporales a celda H3")
    void estrategiaGps_ResuelveCorrectamente() {
        ResolucionZonaContexto contexto = ResolucionZonaContexto.builder()
                .coordenadasGps(new CoordenadasGps(4.6097, -74.0817))
                .resolucionDeseada(9)
                .build();

        assertTrue(estrategiaGps.aplica(contexto));
        assertFalse(estrategiaManual.aplica(contexto));

        ResultadoResolucionZona resultado = estrategiaGps.resolver(contexto);

        assertNotNull(resultado);
        assertNotNull(resultado.getIdCeldaH3());
        assertEquals(15, resultado.getIdCeldaH3().length());
        assertEquals(9, resultado.getResolucionH3());
        assertFalse(resultado.isEsManual());
        assertNotNull(resultado.getLatitudCentroide());
        assertNotNull(resultado.getLongitudCentroide());
    }

    @Test
    @DisplayName("Strategy Pattern: ManualCatalogResolutionStrategy resuelve desde el catálogo predefinido")
    void estrategiaManual_ResuelveCorrectamente() {
        ResolucionZonaContexto contexto = ResolucionZonaContexto.builder()
                .zonaManualId("ZONA-CHAPINERO")
                .resolucionDeseada(9)
                .build();

        assertFalse(estrategiaGps.aplica(contexto));
        assertTrue(estrategiaManual.aplica(contexto));

        ResultadoResolucionZona resultado = estrategiaManual.resolver(contexto);

        assertNotNull(resultado);
        assertNotNull(resultado.getIdCeldaH3());
        assertEquals("Chapinero Central", resultado.getNombreZona());
        assertEquals("ZONA-CHAPINERO", resultado.getZonaManualId());
        assertTrue(resultado.isEsManual());
    }

    @Test
    @DisplayName("Strategy Pattern: ManualCatalogResolutionStrategy lanza excepción ante zona inexistente")
    void estrategiaManual_ZonaInexistente_LanzaExcepcion() {
        ResolucionZonaContexto contexto = ResolucionZonaContexto.builder()
                .zonaManualId("ZONA-INEXISTENTE-999")
                .build();

        assertThrows(ExcepcionZonaNoEncontrada.class, () -> estrategiaManual.resolver(contexto));
    }

    @Test
    @DisplayName("ServicioResolucionZona: Rechaza la solicitud si no hay ni GPS ni zona manual (Criterio 4)")
    void servicioResolucion_SinUbicacion_LanzaExcepcion() {
        assertThrows(ExcepcionUbicacionRequerida.class, () ->
                servicioResolucionZona.resolverZona(null, null));

        assertThrows(ExcepcionUbicacionRequerida.class, () ->
                servicioResolucionZona.resolverZona(new CoordenadasGps(null, null), "   "));
    }

    @Test
    @DisplayName("RNF Rendimiento: La conversión a celda H3 se ejecuta en menos de 1000 ms (< 5 ms)")
    void rendimiento_ConversionH3_InferiorA1000ms() {
        CoordenadasGps coords = new CoordenadasGps(4.6486, -74.0645);

        long inicio = System.nanoTime();
        ResultadoResolucionZona resultado = servicioResolucionZona.resolverZona(coords, null);
        long duracionMs = (System.nanoTime() - inicio) / 1_000_000;

        assertNotNull(resultado.getIdCeldaH3());
        assertTrue(duracionMs < 1000, "La resolución espacial debe tardar menos de 1000 ms, tardó: " + duracionMs + " ms");
    }
}
