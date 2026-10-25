-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V9: SEMILLAS DEMO DE CLIENTES INSTITUCIONALES (HU-15)
-- Compatible con PostgreSQL y H2 (MODE=PostgreSQL)
-- ==============================================================================

-- 1. Institución 1: Alcaldía de Popayán (PENDIENTE_APROBACION)
INSERT INTO cliente_institucional (
    id_usuario, correo, password_hash, activo, razon_social,
    identificacion_fiscal_nit, nombre_contacto, telefono_contacto,
    sector_actividad, estado_cuenta, correo_verificado, token_verificacion,
    rol_nombre, fecha_registro
) VALUES (
    '55555555-5555-5555-5555-555555555501',
    'planeacion@popayan.gov.co',
    '65536:WbN3fO+bNq/GZvZVpI5n5Q==:Nn3y+fH7mR2u5n6yXzV8pQw1yF5o6j0mZz4r6w3a2b1=',
    TRUE,
    'Alcaldía de Popayán - Secretaría de Planeación',
    '891500761-1',
    'Dra. Claudia Patricia Martínez',
    '+57 310 456 7890',
    'GOBIERNO_Y_ADMINISTRACION',
    'PENDIENTE_APROBACION',
    FALSE,
    'TOKEN-VERIF-POPAYAN-01',
    'ROLE_INSTITUTIONAL',
    CURRENT_TIMESTAMP
);

-- Suscripción inicial preasignada para Institución 1
INSERT INTO suscripcion_institucional (
    id_suscripcion, id_institucion, id_plan, estado_suscripcion,
    fecha_inicio, fecha_expiracion, fecha_actualizacion
) VALUES (
    '66666666-6666-6666-6666-666666666601',
    '55555555-5555-5555-5555-555555555501',
    '33333333-3333-3333-3333-333333333303', -- PLAN_ENTERPRISE
    'PENDIENTE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '365' DAY,
    CURRENT_TIMESTAMP
);

-- 2. Institución 2: Universidad del Cauca (ACTIVA)
INSERT INTO cliente_institucional (
    id_usuario, correo, password_hash, activo, razon_social,
    identificacion_fiscal_nit, nombre_contacto, telefono_contacto,
    sector_actividad, estado_cuenta, correo_verificado, token_verificacion,
    rol_nombre, fecha_registro
) VALUES (
    '55555555-5555-5555-5555-555555555502',
    'investigacion@unicauca.edu.co',
    '65536:WbN3fO+bNq/GZvZVpI5n5Q==:Nn3y+fH7mR2u5n6yXzV8pQw1yF5o6j0mZz4r6w3a2b1=',
    TRUE,
    'Universidad del Cauca - Vicerrectoría de Investigaciones',
    '891500128-5',
    'Dr. Rodrigo Restrepo Gómez',
    '+57 312 890 1234',
    'EDUCACION_SUPERIOR',
    'ACTIVA',
    TRUE,
    NULL,
    'ROLE_INSTITUTIONAL',
    CURRENT_TIMESTAMP
);

INSERT INTO suscripcion_institucional (
    id_suscripcion, id_institucion, id_plan, estado_suscripcion,
    fecha_inicio, fecha_expiracion, fecha_actualizacion
) VALUES (
    '66666666-6666-6666-6666-666666666602',
    '55555555-5555-5555-5555-555555555502',
    '33333333-3333-3333-3333-333333333302', -- PLAN_STANDARD
    'ACTIVA',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30' DAY,
    CURRENT_TIMESTAMP
);

-- 3. Institución 3: Cámara de Comercio del Cauca (SUSPENDIDA)
INSERT INTO cliente_institucional (
    id_usuario, correo, password_hash, activo, razon_social,
    identificacion_fiscal_nit, nombre_contacto, telefono_contacto,
    sector_actividad, estado_cuenta, correo_verificado, token_verificacion,
    rol_nombre, fecha_registro
) VALUES (
    '55555555-5555-5555-5555-555555555503',
    'direccion@cccauca.org.co',
    '65536:WbN3fO+bNq/GZvZVpI5n5Q==:Nn3y+fH7mR2u5n6yXzV8pQw1yF5o6j0mZz4r6w3a2b1=',
    TRUE,
    'Cámara de Comercio del Cauca',
    '891500054-2',
    'Dra. Ana Lucía Muñoz',
    '+57 315 234 5678',
    'GREMIAL_Y_EMPRESARIAL',
    'SUSPENDIDA',
    TRUE,
    NULL,
    'ROLE_INSTITUTIONAL',
    CURRENT_TIMESTAMP
);

INSERT INTO suscripcion_institucional (
    id_suscripcion, id_institucion, id_plan, estado_suscripcion,
    fecha_inicio, fecha_expiracion, fecha_actualizacion
) VALUES (
    '66666666-6666-6666-6666-666666666603',
    '55555555-5555-5555-5555-555555555503',
    '33333333-3333-3333-3333-333333333301', -- PLAN_FREE
    'SUSPENDIDA',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30' DAY,
    CURRENT_TIMESTAMP
);

-- 4. Institución 4: Hospital Universitario San José (PENDIENTE_APROBACION)
INSERT INTO cliente_institucional (
    id_usuario, correo, password_hash, activo, razon_social,
    identificacion_fiscal_nit, nombre_contacto, telefono_contacto,
    sector_actividad, estado_cuenta, correo_verificado, token_verificacion,
    rol_nombre, fecha_registro
) VALUES (
    '55555555-5555-5555-5555-555555555504',
    'gerencia@hospitalsanjose.gov.co',
    '65536:WbN3fO+bNq/GZvZVpI5n5Q==:Nn3y+fH7mR2u5n6yXzV8pQw1yF5o6j0mZz4r6w3a2b1=',
    TRUE,
    'Hospital Universitario San José de Popayán E.S.E.',
    '891500333-7',
    'Dr. Carlos Alberto Valencia',
    '+57 318 678 9012',
    'SALUD_PUBLICA',
    'PENDIENTE_APROBACION',
    FALSE,
    'TOKEN-VERIF-SANJOSE-04',
    'ROLE_INSTITUTIONAL',
    CURRENT_TIMESTAMP
);

INSERT INTO suscripcion_institucional (
    id_suscripcion, id_institucion, id_plan, estado_suscripcion,
    fecha_inicio, fecha_expiracion, fecha_actualizacion
) VALUES (
    '66666666-6666-6666-6666-666666666604',
    '55555555-5555-5555-5555-555555555504',
    '33333333-3333-3333-3333-333333333302', -- PLAN_STANDARD
    'PENDIENTE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP + INTERVAL '30' DAY,
    CURRENT_TIMESTAMP
);
