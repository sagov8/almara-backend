package com.almara.modules.admin.security;

import com.almara.common.ExcepcionReglaNegocioInstitucional;
import com.almara.modules.admin.models.Administrador;
import com.almara.modules.institutional.security.ServicioRevocacionToken;
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
 * Proveedor y emisor de tokens JWT administrativos (HU-11).
 * Implementa el patrón Factory Method / Token Builder para centralizar la creación y validación criptográfica
 * de tokens con firma HMAC-SHA256, verificando integridad y estado en la lista de revocación (HU-18).
 */
@Component
public class ProveedorTokenJwtAdmin {

    private final String secretoJwt;
    private final long tiempoExpiracionSegundos;
    private final ObjectMapper objectMapper;
    private final ServicioRevocacionToken servicioRevocacionToken;

    public ProveedorTokenJwtAdmin(
            @Value("${almara.seguridad.admin-jwt-secret:almara-admin-super-secure-secret-key-2026-popayan}") String secretoJwt,
            @Value("${almara.seguridad.admin-jwt-expiracion-segundos:28800}") long tiempoExpiracionSegundos,
            ObjectMapper objectMapper,
            ServicioRevocacionToken servicioRevocacionToken) {
        this.secretoJwt = secretoJwt;
        this.tiempoExpiracionSegundos = tiempoExpiracionSegundos;
        this.objectMapper = objectMapper;
        this.servicioRevocacionToken = servicioRevocacionToken;
    }

    /**
     * Construye y firma criptográficamente un token JWT para el administrador autenticado.
     */
    public String generarToken(Administrador admin) {
        long ahoraSegundos = Instant.now().getEpochSecond();
        long expiraSegundos = ahoraSegundos + tiempoExpiracionSegundos;
        String jti = UUID.randomUUID().toString();

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("alg", "HS256");
        headerMap.put("typ", "JWT");

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("jti", jti);
        payloadMap.put("sub", admin.getIdUsuario().toString());
        payloadMap.put("correo", admin.getCorreo());
        payloadMap.put("nombreCompleto", admin.getNombreCompleto());
        payloadMap.put("rol", admin.getRolNombre());
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
            throw new IllegalStateException("Error al generar token JWT para administrador", e);
        }
    }

    /**
     * Valida la firma, integridad, expiración y no-revocación del token JWT administrativo.
     */
    public ClaimsTokenAdmin validarYExtraerClaims(String token) {
        if (token == null || !token.contains(".")) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_ADMIN_INVALIDO", "El token de administrador no tiene formato JWT válido");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_ADMIN_INVALIDO", "El token JWT de administrador no contiene las tres secciones requeridas");
        }

        String headerEncoded = partes[0];
        String payloadEncoded = partes[1];
        String firmaEncoded = partes[2];

        String dataFirmada = headerEncoded + "." + payloadEncoded;
        byte[] firmaCalculada = firmarHmacSha256(dataFirmada);
        byte[] firmaRecibida = Base64.getUrlDecoder().decode(firmaEncoded);

        if (!MessageDigest.isEqual(firmaCalculada, firmaRecibida)) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "FIRMA_TOKEN_ADMIN_INVALIDA", "La firma del token administrativo no es válida o fue adulterada");
        }

        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(payloadEncoded);
            JsonNode payloadNode = objectMapper.readTree(payloadBytes);

            long expiraSegundos = payloadNode.path("exp").asLong(0);
            long ahoraSegundos = Instant.now().getEpochSecond();

            if (ahoraSegundos >= expiraSegundos) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "SESION_ADMIN_EXPIRADA", "La sesión administrativa ha expirado. Por favor, inicie sesión nuevamente.");
            }

            String jti = payloadNode.path("jti").asText();
            if (servicioRevocacionToken.estaRevocado(jti) || servicioRevocacionToken.estaRevocado(token)) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "SESION_ADMIN_REVOCADA", "La sesión administrativa ha sido cerrada o revocada");
            }

            String rol = payloadNode.path("rol").asText();
            if (!"ROLE_ADMIN".equalsIgnoreCase(rol)) {
                throw new ExcepcionReglaNegocioInstitucional(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "El token no posee privilegios administrativos");
            }

            UUID idAdmin = UUID.fromString(payloadNode.path("sub").asText());
            String correo = payloadNode.path("correo").asText();
            String nombreCompleto = payloadNode.path("nombreCompleto").asText();
            long iat = payloadNode.path("iat").asLong(0);

            return new ClaimsTokenAdmin(idAdmin, correo, nombreCompleto, rol, jti, iat, expiraSegundos);
        } catch (ExcepcionReglaNegocioInstitucional e) {
            throw e;
        } catch (Exception e) {
            throw new ExcepcionReglaNegocioInstitucional(HttpStatus.UNAUTHORIZED, "TOKEN_ADMIN_CORRUPTO", "Error al procesar el contenido del token administrativo: " + e.getMessage());
        }
    }

    public long getTiempoExpiracionSegundos() {
        return tiempoExpiracionSegundos;
    }

    private byte[] firmarHmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretoJwt.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular firma HMAC-SHA256", e);
        }
    }
}
