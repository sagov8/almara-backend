package com.almara.modules.admin.security;

import com.almara.modules.institutional.security.UtilidadPasswordHash;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Implementación concreta del patrón Strategy de hashing usando PBKDF2WithHmacSHA256 con salt aleatorio.
 */
@Component
@Primary
public class EstrategiaPBKDF2PasswordHash implements EstrategiaPasswordHash {

    private final UtilidadPasswordHash utilidadPasswordHash;

    public EstrategiaPBKDF2PasswordHash(UtilidadPasswordHash utilidadPasswordHash) {
        this.utilidadPasswordHash = utilidadPasswordHash;
    }

    @Override
    public String hashear(String passwordPlano) {
        return utilidadPasswordHash.hashearPassword(passwordPlano);
    }

    @Override
    public boolean verificar(String passwordPlano, String hashAlmacenado) {
        return utilidadPasswordHash.verificar(passwordPlano, hashAlmacenado);
    }
}
