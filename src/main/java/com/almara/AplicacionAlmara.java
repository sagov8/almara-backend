package com.almara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal de inicio para el backend de Almara.
 * Habilita ejecución asíncrona para el soporte del patrón Publicador/Suscriptor.
 */
@SpringBootApplication
@EnableAsync
public class AplicacionAlmara {

    public static void main(String[] args) {
        SpringApplication.run(AplicacionAlmara.class, args);
    }
}
