package com.almara.modules.emotion.services;

import com.almara.common.ExcepcionSolicitudesExcesivas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Servicio encargado del control de frecuencia (Rate Limiting y unicidad) por token temporal.
 * Cumple con el Criterio 1: "La emoción escogida no debe ser cambiada en un lapso de tiempo determinado".
 * 
 * Implementa soporte dual:
 * 1. Redis con TTL para despliegue distribuido de alto rendimiento.
 * 2. Fallback local en memoria concurrente para resiliencia y pruebas unitarias aisladas.
 */
@Service
public class ServicioControlFrecuencia {

    private static final Logger registroLog = LoggerFactory.getLogger(ServicioControlFrecuencia.class);
    private static final String PREFIJO_CLAVE = "almara:bloqueo:emocion:";

    private final long segundosBloqueo;
    private final StringRedisTemplate plantillaRedis;
    private final ConcurrentMap<String, Instant> cacheMemoriaFallback = new ConcurrentHashMap<>();

    public ServicioControlFrecuencia(
            @Value("${almara.emocion.segundos-bloqueo:900}") long segundosBloqueo,
            @Autowired(required = false) StringRedisTemplate plantillaRedis) {
        this.segundosBloqueo = segundosBloqueo;
        this.plantillaRedis = plantillaRedis;
    }

    /**
     * Valida si el token de sesión anónimo está en período de bloqueo.
     * Si está bloqueado, arroja ExcepcionSolicitudesExcesivas con los segundos restantes.
     */
    public void validarDisponibilidad(String tokenSesionTemporal) {
        String clave = PREFIJO_CLAVE + tokenSesionTemporal;

        if (plantillaRedis != null) {
            try {
                Long ttlSegundos = plantillaRedis.getExpire(clave);
                if (ttlSegundos != null && ttlSegundos > 0) {
                    throw new ExcepcionSolicitudesExcesivas(
                            "Ya has registrado una emoción recientemente. Por favor espera antes de enviar otra.",
                            ttlSegundos);
                }
                return;
            } catch (ExcepcionSolicitudesExcesivas ex) {
                throw ex;
            } catch (Exception ex) {
                registroLog.warn("Redis no disponible para validar frecuencia, usando fallback en memoria: {}", ex.getMessage());
            }
        }

        // Fallback en memoria local
        Instant expiracion = cacheMemoriaFallback.get(tokenSesionTemporal);
        if (expiracion != null) {
            Instant ahora = Instant.now();
            if (ahora.isBefore(expiracion)) {
                long segundosRestantes = Duration.between(ahora, expiracion).getSeconds();
                throw new ExcepcionSolicitudesExcesivas(
                        "Ya has registrado una emoción recientemente. Por favor espera antes de enviar otra.",
                        Math.max(1, segundosRestantes));
            } else {
                cacheMemoriaFallback.remove(tokenSesionTemporal);
            }
        }
    }

    /**
     * Aplica el bloqueo temporal para el token de sesión anónimo tras registrar la emoción.
     */
    public void registrarBloqueo(String tokenSesionTemporal) {
        String clave = PREFIJO_CLAVE + tokenSesionTemporal;
        Instant expiracion = Instant.now().plusSeconds(segundosBloqueo);

        if (plantillaRedis != null) {
            try {
                plantillaRedis.opsForValue().set(clave, "BLOQUEADO", Duration.ofSeconds(segundosBloqueo));
                return;
            } catch (Exception ex) {
                registroLog.warn("Redis no disponible para registrar bloqueo, usando fallback en memoria: {}", ex.getMessage());
            }
        }

        cacheMemoriaFallback.put(tokenSesionTemporal, expiracion);
    }

    public long obtenerSegundosBloqueo() {
        return segundosBloqueo;
    }

    /**
     * Método de utilidad para reiniciar estado en pruebas automatizadas.
     */
    public void limpiarBloqueos() {
        cacheMemoriaFallback.clear();
        if (plantillaRedis != null) {
            try {
                var claves = plantillaRedis.keys(PREFIJO_CLAVE + "*");
                if (claves != null && !claves.isEmpty()) {
                    plantillaRedis.delete(claves);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
