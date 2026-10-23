package com.almara.modules.institutional.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio encargado de la gestión de la lista negra (blacklist) y revocación inmediata
 * de tokens JWT de clientes institucionales (HU-18: include Revocar Token de Acceso).
 */
@Service
public class ServicioRevocacionToken {

    private static final Logger log = LoggerFactory.getLogger(ServicioRevocacionToken.class);

    /**
     * Almacenamiento en memoria concurrente con clave (token o jti) y valor de expiración.
     */
    private final Map<String, Instant> tokensRevocados = new ConcurrentHashMap<>();

    /**
     * Registra un token o identificador de token (jti) en la lista negra hasta su fecha de expiración.
     *
     * @param identificadorToken Token JWT completo o jti
     * @param fechaExpiracion    Momento en el que expira el token de forma natural
     */
    public void revocarToken(String identificadorToken, Instant fechaExpiracion) {
        if (identificadorToken == null || identificadorToken.isBlank()) {
            return;
        }
        Instant expiracionEfectiva = fechaExpiracion != null
                ? fechaExpiracion
                : Instant.now().plusSeconds(86400); // 24 horas por defecto

        tokensRevocados.put(identificadorToken.trim(), expiracionEfectiva);
        log.info("Token o jti revocado satisfactoriamente en blacklist: expiraEn={}", expiracionEfectiva);
    }

    /**
     * Comprueba si un token o identificador de token (jti) ha sido revocado.
     *
     * @param identificadorToken Token JWT completo o jti
     * @return true si el token fue revocado previamente y no ha caducado
     */
    public boolean estaRevocado(String identificadorToken) {
        if (identificadorToken == null || identificadorToken.isBlank()) {
            return false;
        }

        Instant expiracion = tokensRevocados.get(identificadorToken.trim());
        if (expiracion == null) {
            return false;
        }

        // Si ya expiró de forma natural, podemos eliminarlo del mapa y considerar que está inactivo
        if (Instant.now().isAfter(expiracion)) {
            tokensRevocados.remove(identificadorToken.trim());
            return true; // Sigue siendo inválido por expiración previa
        }

        return true;
    }

    /**
     * Tarea programada o invocable para limpiar tokens cuyo tiempo de vida natural ya haya pasado.
     */
    @Scheduled(fixedRate = 3600000) // Cada hora
    public void limpiarExpirados() {
        Instant ahora = Instant.now();
        tokensRevocados.entrySet().removeIf(entry -> ahora.isAfter(entry.getValue()));
        log.debug("Limpieza de tokens revocados expirados finalizada. Tamaño actual={}", tokensRevocados.size());
    }

    /**
     * Retorna el número de tokens actualmente registrados en la lista negra.
     */
    public int totalTokensRevocados() {
        return tokensRevocados.size();
    }
}
