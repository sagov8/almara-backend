-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V7: CONFIGURACIÓN DINÁMICA DE UMBRALES DEL MAPA (HU-13)
-- Parámetros de k-anonimato, privacidad ciudadana y refresco en caliente
-- Compatible con PostgreSQL y H2 (MODE=PostgreSQL)
-- ==============================================================================

CREATE TABLE IF NOT EXISTS configuracion_mapa_umbral (
    id VARCHAR(50) PRIMARY KEY,
    umbral_minimo_k INT NOT NULL DEFAULT 5,
    tiempo_recalculo_segundos INT NOT NULL DEFAULT 60,
    modo_estricto BOOLEAN NOT NULL DEFAULT TRUE,
    mensaje_proteccion VARCHAR(500) NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA'
);

-- Semilla con la configuración global por defecto de la plataforma
INSERT INTO configuracion_mapa_umbral (
    id,
    umbral_minimo_k,
    tiempo_recalculo_segundos,
    modo_estricto,
    mensaje_proteccion,
    fecha_actualizacion,
    actualizado_por
) VALUES (
    'GLOBAL_POPAYAN',
    5,
    60,
    TRUE,
    'Protección en Comunidad: Para cuidar tu privacidad, las zonas solo se colorean en el mapa cuando al menos 5 vecinos han compartido cómo se sienten en ese lugar. Tu participación es 100% anónima.',
    CURRENT_TIMESTAMP,
    'SISTEMA'
);
