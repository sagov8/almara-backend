package com.almara.modules.emotion.repositories;

import com.almara.modules.aggregation.models.EntidadEstadoEmocionalZona;
import com.almara.modules.aggregation.repositories.RepositorioEstadoEmocionalZonaJpa;
import com.almara.modules.emotion.models.EntidadReporteEmocion;
import com.almara.modules.emotion.models.RegistroEmocionRespuesta;
import com.almara.modules.emotion.models.RegistroEmocionSolicitud;
import com.almara.modules.emotion.models.RegistroIntensidadSolicitud;
import com.almara.modules.emotion.models.TipoEmocion;
import com.almara.modules.emotion.services.ServicioControlFrecuencia;
import com.almara.modules.emotion.services.ServicioRegistroEmocion;
import com.almara.modules.emotion.services.ServicioRegistroIntensidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PersistenciaReporteYEstadoEmocionalPrueba {

    @Autowired
    private ServicioRegistroEmocion servicioRegistroEmocion;

    @Autowired
    private ServicioRegistroIntensidad servicioRegistroIntensidad;

    @Autowired
    private ServicioControlFrecuencia servicioControlFrecuencia;

    @Autowired
    private RepositorioReporteEmocion repositorioReporteEmocion;

    @Autowired
    private RepositorioReporteEmocionJpa repositorioReporteEmocionJpa;

    @Autowired
    private RepositorioEstadoEmocionalZonaJpa repositorioEstadoEmocionalZonaJpa;

    @BeforeEach
    void prepararPrueba() {
        servicioControlFrecuencia.limpiarBloqueos();
        repositorioReporteEmocion.limpiar();
    }

    @Test
    @DisplayName("Persistencia: El reporte de emoción se guarda efectivamente en la tabla reporte_emocion")
    void registrarEmocion_PersisteEnBaseDeDatos() {
        String token = "token-persistencia-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.FELICIDAD)
                .tokenSesionTemporal(token)
                .zonaManualId("ZONA-PARQUE-CALDAS")
                .build();

        RegistroEmocionRespuesta respuesta = servicioRegistroEmocion.registrarEmocion(solicitud);

        assertNotNull(respuesta.getIdEvento());
        Optional<EntidadReporteEmocion> enBaseDatos = repositorioReporteEmocionJpa.findById(respuesta.getIdEvento());
        assertTrue(enBaseDatos.isPresent(), "El reporte debe persistirse físicamente en la tabla reporte_emocion");

        EntidadReporteEmocion entidad = enBaseDatos.get();
        assertEquals("FELICIDAD", entidad.getIdEmocion());
        assertEquals(respuesta.getIdCeldaH3(), entidad.getIdCeldaH3());
        assertEquals(token, entidad.getTokenSesionTemporal());
        assertNotNull(entidad.getFechaHora());
    }

    @Test
    @DisplayName("Persistencia: La intensidad indicada actualiza el registro en la base de datos")
    void registrarIntensidad_ActualizaEnBaseDeDatos() {
        String token = "token-persistencia-int-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.ANSIEDAD)
                .tokenSesionTemporal(token)
                .zonaManualId("ZONA-HUMILLADERO")
                .build();

        RegistroEmocionRespuesta respuestaEmocion = servicioRegistroEmocion.registrarEmocion(solicitud);

        RegistroIntensidadSolicitud solicitudInt = RegistroIntensidadSolicitud.builder()
                .idEvento(respuestaEmocion.getIdEvento())
                .nivelIntensidad(4)
                .build();

        servicioRegistroIntensidad.registrarIntensidad(solicitudInt);

        Optional<EntidadReporteEmocion> entidadActualizada = repositorioReporteEmocionJpa.findById(respuestaEmocion.getIdEvento());
        assertTrue(entidadActualizada.isPresent());
        assertEquals(4.0f, entidadActualizada.get().getIntensidad());
    }

    @Test
    @DisplayName("Persistencia: El estado emocional agregado se persiste en la tabla estado_emocional_zona")
    void eventoEmocion_PersisteEstadoEnTablaEstadoEmocionalZona() {
        String token = "token-persistencia-zona-" + UUID.randomUUID();
        RegistroEmocionSolicitud solicitud = RegistroEmocionSolicitud.builder()
                .emocion(TipoEmocion.FELICIDAD)
                .tokenSesionTemporal(token)
                .zonaManualId("ZONA-PARQUE-CALDAS")
                .build();

        RegistroEmocionRespuesta respuesta = servicioRegistroEmocion.registrarEmocion(solicitud);

        Optional<EntidadEstadoEmocionalZona> estadoZona = repositorioEstadoEmocionalZonaJpa.findById(respuesta.getIdCeldaH3());
        assertTrue(estadoZona.isPresent(), "La celda debe registrar un snapshot en la tabla estado_emocional_zona");

        EntidadEstadoEmocionalZona entidad = estadoZona.get();
        assertEquals(respuesta.getIdCeldaH3(), entidad.getIdCeldaH3());
        assertNotNull(entidad.getCodigoHexColor());
        assertNotNull(entidad.getTotalEventosPeriodo());
        assertTrue(entidad.getTotalEventosPeriodo() >= 1);
    }
}
