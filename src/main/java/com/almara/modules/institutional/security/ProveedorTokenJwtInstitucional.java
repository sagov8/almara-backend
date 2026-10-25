package com.almara.modules.institutional.security;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.institutional.models.ClienteInstitucional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Proveedor y validador de tokens JWT estándar (HMAC-SHA256) nativo de Java 21 sin dependencias externas.
 */
@Component
public class ProveedorTokenJwtInstitucional {

    private final String secretoJwt;
    private final long tiempoExpiracionSegundos;
    private final ObjectMapper objectMapper;
    private final ServicioRevocacionToken servicioRevocacionToken;

    public ProveedorTokenJwtInstitucional(
            @Value("${almara.seguridad.jwt-secret:almara-institutional-secret-key-2026-urban-intelligence}") String secretoJwt,
            @Value("${almara.seguridad.jwt-expiracion-segundos:28800}") long tiempoExpiracionSegundos,
            ObjectMapper objectMapper,
            ServicioRevocacionToken servicioRevocacionToken) {
        this.secretoJwt = secretoJwt;
        this.tiempoExpiracionSegundos = tiempoExpiracionSegundos;
        this.objectMapper = objectMapper;
        this.servicioRevocacionToken = servicioRevocacionToken;
    }

    public String generarToken(ClienteInstitucional cliente, String codigoPlan, String estadoSuscripcion) {
        long ahoraSegundos = Instant.now().getEpochSecond();
        long expiraSegundos = ahoraSegundos + tiempoExpiracionSegundos;
        String jti = UUID.randomUUID().toString();

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", "HS256");
        headerMap.put("typ", "JWT");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("jti", jti);
        payloadMap.put("sub", cliente.getIdUsuario().toString());
        payloadMap.put("correo", cliente.getCorreo());
        payloadMap.put("razonSocial", cliente.getRazonSocial());
        payloadMap.put("rol", cliente.getRolNombre());
        payloadMap.put("codigoPlan", codigoPlan != null ? codigoPlan : "PLAN_FREE");
        payloadMap.put("estadoSuscripcion", estadoSuscripcion != null ? estadoSuscripcion : "ACTIVA");
        payloadMap.put("iat", ahoraSegundos);
        payloadMap.put("exp", expiraSegundos);

        try {
            String headerJson = objectMapper.writeValueAsString(headerMap);
            String payloadJson = objectMapper.writeValueAsString(payloadMap);

            String headerEncoded = encoder.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));

            String dataParaFirmar = headerEncoded + "." + payloadEncoded;
            String firmaEncoded = encoder.encodeToString(firmarHmacSha256(dataParaFirmar));

            return dataParaFirmar + "." + firmaEncoded;
        } catch (Exception e) {
            throw new IllegalStateException("Error al generar token JWT institucional", e);
        }
    }

    public ClaimsTokenInstitucional validarYExtraerClaims(String token) {
        if (token == null || !token.contains(".")) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "El token proporcionado no tiene formato JWT válido");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "El token JWT no contiene las tres secciones requeridas");
        }

        String headerEncoded = partes[0];
        String payloadEncoded = partes[1];
        String firmaEncoded = partes[2];

        String dataFirmada = headerEncoded + "." + payloadEncoded;
        byte[] firmaCalculada = firmarHmacSha256(dataFirmada);
        byte[] firmaRecibida = Base64.getUrlDecoder().decode(firmaEncoded);

        if (!MessageDigest.isEqual(firmaCalculada, firmaRecibida)) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "FIRMA_TOKEN_INVALIDA", "La firma del token JWT no es válida o fue alterada");
        }

        if (servicioRevocacionToken != null && servicioRevocacionToken.estaRevocado(token)) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_REVOCADO", "El token de acceso ha sido revocado. Debe iniciar sesión nuevamente.");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadEncoded);
            JsonNode jsonNode = objectMapper.readTree(payloadBytes);

            long exp = jsonNode.path("exp").asLong(0);
            if (exp > 0 && exp < Instant.now().getEpochSecond()) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRADO", "El token JWT ha expirado");
            }

            String jti = jsonNode.path("jti").asText();
            if (servicioRevocacionToken != null && jti != null && !jti.isBlank() && servicioRevocacionToken.estaRevocado(jti)) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_REVOCADO", "El token de acceso ha sido revocado. Debe iniciar sesión nuevamente.");
            }

            UUID idUsuario = UUID.fromString(jsonNode.path("sub").asText());
            String correo = jsonNode.path("correo").asText();
            String razonSocial = jsonNode.path("razonSocial").asText();
            String rol = jsonNode.path("rol").asText();
            String codigoPlan = jsonNode.path("codigoPlan").asText();
            String estadoSuscripcion = jsonNode.path("estadoSuscripcion").asText();
            long iat = jsonNode.path("iat").asLong();

            if (servicioRevocacionToken != null && servicioRevocacionToken.estaSesionUsuarioRevocada(idUsuario, iat)) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_REVOCADO",
                        "Las sesiones activas de esta institución han sido revocadas por el administrador. Inicie sesión nuevamente.");
            }

            return new ClaimsTokenInstitucional(jti, idUsuario, correo, razonSocial, rol, codigoPlan, estadoSuscripcion, iat, exp);
        } catch (ExcepcionReglaNegocioInstitucional ex) {
            throw ex;
        } catch (Exception e) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "PAYLOAD_TOKEN_INVALIDO", "No fue posible deserializar los claims del token JWT");
        }
    }

    public long getTiempoExpiracionSegundos() {
        return tiempoExpiracionSegundos;
    }

    private byte[] firmarHmacSha256(String data) {
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretoJwt.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            return hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular firma HMAC-SHA256", e);
        }
    }
}
