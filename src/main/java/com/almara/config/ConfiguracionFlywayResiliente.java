package com.almara.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración resiliente de migraciones de base de datos con Flyway.
 * Ejecuta automáticamente {@code flyway.repair()} antes de {@code flyway.migrate()}
 * para alinear posibles diferencias de checksum (por ejemplo, saltos de línea CRLF/LF
 * entre sistemas operativos o ajustes en scripts previos) y limpiar registros fallidos
 * antes de aplicar nuevas migraciones.
 */
@Configuration
public class ConfiguracionFlywayResiliente {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionFlywayResiliente.class);

    @Bean
    public FlywayMigrationStrategy estrategiaMigracionFlyway() {
        return flyway -> {
            try {
                log.info("Ejecutando reparación preventiva de historial Flyway (checksums y estados)...");
                flyway.repair();
            } catch (Exception ex) {
                log.warn("Aviso en reparación preventiva de Flyway: {}", ex.getMessage());
            }
            flyway.migrate();
        };
    }
}
