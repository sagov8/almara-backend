-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V5: MÓDULO CLIENTE INSTITUCIONAL Y SUSCRIPCIONES SAAS
-- Compatible con PostgreSQL y H2 (MODE=PostgreSQL)
-- ==============================================================================

-- 1. Roles del Sistema (RBAC)
CREATE TABLE IF NOT EXISTS rol_sistema (
    id_rol UUID PRIMARY KEY,
    nombre_rol VARCHAR(40) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

-- 2. Clientes Institucionales (Herencia de Usuario)
CREATE TABLE IF NOT EXISTS cliente_institucional (
    id_usuario UUID PRIMARY KEY,
    correo VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    razon_social VARCHAR(150) NOT NULL,
    identificacion_fiscal_nit VARCHAR(30) NOT NULL UNIQUE,
    nombre_contacto VARCHAR(80) NOT NULL,
    telefono_contacto VARCHAR(20),
    sector_actividad VARCHAR(30),
    estado_cuenta VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_APROBACION',
    correo_verificado BOOLEAN NOT NULL DEFAULT FALSE,
    token_verificacion VARCHAR(100),
    rol_nombre VARCHAR(40) NOT NULL DEFAULT 'ROLE_INSTITUTIONAL',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Módulos del Sistema (HU-20)
CREATE TABLE IF NOT EXISTS modulo_sistema (
    id_modulo UUID PRIMARY KEY,
    codigo_modulo VARCHAR(50) NOT NULL UNIQUE,
    nombre_modulo VARCHAR(80) NOT NULL,
    descripcion VARCHAR(200),
    es_premium BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4. Planes de Suscripción SaaS (Herencia de ProductoServicio - HU-19)
CREATE TABLE IF NOT EXISTS plan_suscripcion (
    id_producto UUID PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    precio_base DECIMAL(12, 2) NOT NULL,
    tipo_plan VARCHAR(30) NOT NULL,
    duracion_dias INT NOT NULL,
    es_gratuito BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. Matriz de Permisos por Plan y Módulo (HU-20)
CREATE TABLE IF NOT EXISTS matriz_permiso_modulo (
    id_matriz UUID PRIMARY KEY,
    id_plan UUID NOT NULL REFERENCES plan_suscripcion(id_producto),
    id_modulo UUID NOT NULL REFERENCES modulo_sistema(id_modulo),
    modulo_habilitado BOOLEAN NOT NULL DEFAULT TRUE,
    endpoint_restringido VARCHAR(150) NOT NULL
);

-- 6. Suscripciones Institucionales (HU-20, HU-21)
CREATE TABLE IF NOT EXISTS suscripcion_institucional (
    id_suscripcion UUID PRIMARY KEY,
    id_institucion UUID NOT NULL REFERENCES cliente_institucional(id_usuario),
    id_plan UUID NOT NULL REFERENCES plan_suscripcion(id_producto),
    estado_suscripcion VARCHAR(25) NOT NULL,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. Carrito de Compra e Ítems (HU-22)
CREATE TABLE IF NOT EXISTS carrito_compra (
    id_carrito UUID PRIMARY KEY,
    id_institucion UUID NOT NULL UNIQUE REFERENCES cliente_institucional(id_usuario),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS item_carrito (
    id_item UUID PRIMARY KEY,
    id_carrito UUID NOT NULL REFERENCES carrito_compra(id_carrito) ON DELETE CASCADE,
    id_plan UUID NOT NULL REFERENCES plan_suscripcion(id_producto),
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(12, 2) NOT NULL
);

-- 8. Orden de Pedido y Líneas de Detalle (Composición Fuerte - HU-22)
CREATE TABLE IF NOT EXISTS orden_pedido (
    id_orden UUID PRIMARY KEY,
    numero_orden VARCHAR(40) NOT NULL UNIQUE,
    id_institucion UUID NOT NULL REFERENCES cliente_institucional(id_usuario),
    id_plan_seleccionado UUID NOT NULL REFERENCES plan_suscripcion(id_producto),
    fecha_emision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_orden VARCHAR(25) NOT NULL,
    monto_total DECIMAL(12, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS linea_detalle (
    id_linea UUID PRIMARY KEY,
    id_orden UUID NOT NULL REFERENCES orden_pedido(id_orden) ON DELETE CASCADE,
    id_producto UUID NOT NULL REFERENCES plan_suscripcion(id_producto),
    concepto VARCHAR(150) NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    precio_aplicado DECIMAL(12, 2) NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL
);

-- 9. Pagos (HU-23)
CREATE TABLE IF NOT EXISTS pago_suscripcion (
    id_pago UUID PRIMARY KEY,
    id_orden UUID NOT NULL REFERENCES orden_pedido(id_orden),
    referencia_externa VARCHAR(100),
    monto_pagado DECIMAL(12, 2) NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    estado_pago VARCHAR(25) NOT NULL,
    motivo_rechazo VARCHAR(255),
    fecha_transaccion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. Bitácora de Auditoría Institucional y Suscripciones (HU-20, RNF-13)
CREATE TABLE IF NOT EXISTS auditoria_institucional (
    id_auditoria UUID PRIMARY KEY,
    id_entidad_afectada UUID NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL,
    detalle VARCHAR(255) NOT NULL,
    actor_responsable VARCHAR(100) NOT NULL,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- SEMILLAS INICIALES (ROLES, MÓDULOS, PLANES SAAS Y MATRIZ DE PERMISOS)
-- ==============================================================================
INSERT INTO rol_sistema (id_rol, nombre_rol, descripcion) VALUES
    ('11111111-1111-1111-1111-111111111101', 'ROLE_INSTITUTIONAL', 'Cliente Institucional de la plataforma Almara'),
    ('11111111-1111-1111-1111-111111111102', 'ROLE_ADMIN', 'Administrador global de la plataforma Almara');

INSERT INTO modulo_sistema (id_modulo, codigo_modulo, nombre_modulo, descripcion, es_premium) VALUES
    ('22222222-2222-2222-2222-222222222201', 'MOD_DASHBOARD_BASICO', 'Dashboard Analítico Básico', 'Resumen general del clima emocional colectivo de la ciudad.', FALSE),
    ('22222222-2222-2222-2222-222222222202', 'MOD_FILTROS_TERRITORIALES', 'Filtros por Zonas Territoriales', 'Segmentación avanzada por zonas urbanas de Popayán y celdas H3.', TRUE),
    ('22222222-2222-2222-2222-222222222203', 'MOD_EXPORTACION_REPORTES', 'Exportación y Analítica Avanzada', 'Descarga y comparativa histórica multicelda para toma de decisiones.', TRUE);

INSERT INTO plan_suscripcion (id_producto, codigo, nombre, descripcion, precio_base, tipo_plan, duracion_dias, es_gratuito, activo) VALUES
    ('33333333-3333-3333-3333-333333333301', 'PLAN_FREE', 'Plan Institucional Básico (Freemium)', 'Acceso al panel institucional con vista general sin segmentación territorial.', 0.00, 'FREEMIUM', 30, TRUE, TRUE),
    ('33333333-3333-3333-3333-333333333302', 'PLAN_STANDARD', 'Plan Institucional Estándar', 'Incluye Dashboard Analítico y Filtros de Clima Emocional por Zonas Territoriales.', 149000.00, 'STANDARD', 30, FALSE, TRUE),
    ('33333333-3333-3333-3333-333333333303', 'PLAN_ENTERPRISE', 'Plan Corporativo / Alcaldía Premium', 'Acceso total: Dashboard, Filtros Territoriales H3 y Analítica Avanzada anual.', 1490000.00, 'ENTERPRISE', 365, FALSE, TRUE);

-- Matriz para PLAN_FREE (Solo Dashboard Básico habilitado; Filtros y Exportación restringidos)
INSERT INTO matriz_permiso_modulo (id_matriz, id_plan, id_modulo, modulo_habilitado, endpoint_restringido) VALUES
    ('44444444-4444-4444-4444-444444444401', '33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222201', TRUE, '/api/v1/institutional/dashboard/resumen'),
    ('44444444-4444-4444-4444-444444444402', '33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222202', FALSE, '/api/v1/institutional/dashboard/filtrar-zonas'),
    ('44444444-4444-4444-4444-444444444403', '33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222203', FALSE, '/api/v1/institutional/dashboard/exportar');

-- Matriz para PLAN_STANDARD (Dashboard Básico + Filtros Territoriales habilitados)
INSERT INTO matriz_permiso_modulo (id_matriz, id_plan, id_modulo, modulo_habilitado, endpoint_restringido) VALUES
    ('44444444-4444-4444-4444-444444444404', '33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222201', TRUE, '/api/v1/institutional/dashboard/resumen'),
    ('44444444-4444-4444-4444-444444444405', '33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222202', TRUE, '/api/v1/institutional/dashboard/filtrar-zonas'),
    ('44444444-4444-4444-4444-444444444406', '33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222203', FALSE, '/api/v1/institutional/dashboard/exportar');

-- Matriz para PLAN_ENTERPRISE (Todos los módulos habilitados)
INSERT INTO matriz_permiso_modulo (id_matriz, id_plan, id_modulo, modulo_habilitado, endpoint_restringido) VALUES
    ('44444444-4444-4444-4444-444444444407', '33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222201', TRUE, '/api/v1/institutional/dashboard/resumen'),
    ('44444444-4444-4444-4444-444444444408', '33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222202', TRUE, '/api/v1/institutional/dashboard/filtrar-zonas'),
    ('44444444-4444-4444-4444-444444444409', '33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222203', TRUE, '/api/v1/institutional/dashboard/exportar');
