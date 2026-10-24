-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V6: MÓDULO ADMINISTRADOR Y AUDITORÍA (HU-11 A HU-15)
-- Compatible con PostgreSQL y H2 (MODE=PostgreSQL)
-- ==============================================================================

-- 1. Tabla de Usuarios Administradores (Herencia de Usuario)
CREATE TABLE IF NOT EXISTS administrador (
    id_usuario UUID PRIMARY KEY,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    nombre_completo VARCHAR(120) NOT NULL,
    rol_nombre VARCHAR(40) NOT NULL DEFAULT 'ROLE_ADMIN',
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    ultimo_login TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bitácora de Auditoría Administrativa (HU-11 a HU-15, RNF-13)
CREATE TABLE IF NOT EXISTS auditoria_admin (
    id_auditoria UUID PRIMARY KEY,
    id_admin UUID,
    accion VARCHAR(60) NOT NULL,
    detalle VARCHAR(255) NOT NULL,
    actor_responsable VARCHAR(100) NOT NULL,
    ip_origen VARCHAR(60),
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Semilla Inicial: Administrador Principal por Defecto
-- Credenciales preconfiguradas para desarrollo y pruebas:
-- Usuario / Correo: admin@almara.com
-- Contraseña plana: Admin1234*
-- Hash generado con PBKDF2WithHmacSHA256 (65536 iteraciones)
INSERT INTO administrador (
    id_usuario,
    correo,
    password_hash,
    activo,
    nombre_completo,
    rol_nombre,
    intentos_fallidos,
    fecha_creacion
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa01',
    'admin@almara.com',
    '65536:YOVpV3DB67LRd18eo4TpLA==:an3rO3NXaE+RBKUGgnpYzOJuCJ+6GSkQOMsXef9ANIg=',
    TRUE,
    'Administrador Principal Almara',
    'ROLE_ADMIN',
    0,
    CURRENT_TIMESTAMP
);
