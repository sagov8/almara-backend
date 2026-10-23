package com.almara.modules.institutional.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilidad de hashing seguro de contraseñas con salt aleatorio usando PBKDF2WithHmacSHA256 nativo.
 */
@Component
public class UtilidadPasswordHash {

    private static final int ITERACIONES = 65536;
    private static final int LONGITUD_CLAVE = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String hashearPassword(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = generarHash(passwordPlano.toCharArray(), salt, ITERACIONES, LONGITUD_CLAVE);

        return ITERACIONES + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(String passwordPlano, String hashAlmacenado) {
        if (passwordPlano == null || hashAlmacenado == null || !hashAlmacenado.contains(":")) {
            return false;
        }
        String[] partes = hashAlmacenado.split(":");
        if (partes.length != 3) {
            return false;
        }
        try {
            int iteraciones = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[2]);

            byte[] hashCalculado = generarHash(passwordPlano.toCharArray(), salt, iteraciones, hashEsperado.length * 8);

            return slowEquals(hashEsperado, hashCalculado);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] generarHash(char[] password, byte[] salt, int iteraciones, int longitudBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, longitudBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error al generar hash seguro de contraseña", e);
        }
    }

    private boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
