package com.almara.modules.admin.security;

/**
 * Patrón Strategy para la protección, hashing y validación de contraseñas administrativas (HU-11).
 * Permite cambiar o actualizar el algoritmo criptográfico sin afectar los servicios consumidores.
 */
public interface EstrategiaPasswordHash {

    /**
     * Genera el hash criptográfico seguro a partir de la contraseña plana.
     */
    String hashear(String passwordPlano);

    /**
     * Valida si una contraseña en texto plano coincide con el hash almacenado.
     */
    boolean verificar(String passwordPlano, String hashAlmacenado);
}
