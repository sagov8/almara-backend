-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V8: ZONAS GEOGRÁFICAS H3 Y POLÍGONOS POSTGIS (HU-14)
-- Compatible con PostgreSQL/PostGIS y H2 (MODE=PostgreSQL)
-- ==============================================================================

-- 1. Configuración de Resolución Espacial H3 por Defecto
CREATE TABLE IF NOT EXISTS configuracion_espacial_h3 (
    id VARCHAR(50) PRIMARY KEY,
    resolucion_h3_default INT NOT NULL DEFAULT 9,
    resolucion_minima INT NOT NULL DEFAULT 7,
    resolucion_maxima INT NOT NULL DEFAULT 10,
    descripcion VARCHAR(255) NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA'
);

-- Semilla de configuración inicial para Popayán (Resolución 9 por defecto)
INSERT INTO configuracion_espacial_h3 (
    id,
    resolucion_h3_default,
    resolucion_minima,
    resolucion_maxima,
    descripcion,
    fecha_actualizacion,
    actualizado_por
) VALUES (
    'GLOBAL_POPAYAN',
    9,
    7,
    10,
    'Resolución espacial H3 por defecto para el área urbana de Popayán (Resolución 9 ≈ 0.1 km² de cobertura por celda)',
    CURRENT_TIMESTAMP,
    'SISTEMA'
);

-- 2. Tabla de Polígonos Urbanos Espaciales Delimitados (PostGIS / OGC GeoJSON)
CREATE TABLE IF NOT EXISTS poligono_urbano_espacial (
    id_zona VARCHAR(50) PRIMARY KEY,
    nombre_zona VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    geometria_geojson TEXT NOT NULL,
    latitud_centroide DECIMAL(10, 6) NOT NULL,
    longitud_centroide DECIMAL(10, 6) NOT NULL,
    resolucion_h3 INT NOT NULL DEFAULT 9,
    id_celda_h3_centroide VARCHAR(20) NOT NULL,
    total_celdas_h3_cubiertas INT NOT NULL DEFAULT 1,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_por VARCHAR(100) NOT NULL DEFAULT 'SISTEMA'
);

CREATE INDEX IF NOT EXISTS idx_poligono_urbano_nombre ON poligono_urbano_espacial(nombre_zona);
CREATE INDEX IF NOT EXISTS idx_poligono_urbano_celda_centroide ON poligono_urbano_espacial(id_celda_h3_centroide);

-- 3. Semillas de Polígonos Urbanos Delimitados en Popayán (GeoJSON cerrados RFC 7946)
INSERT INTO poligono_urbano_espacial (
    id_zona,
    nombre_zona,
    descripcion,
    geometria_geojson,
    latitud_centroide,
    longitud_centroide,
    resolucion_h3,
    id_celda_h3_centroide,
    total_celdas_h3_cubiertas,
    activo,
    fecha_creacion,
    actualizado_por
) VALUES (
    'ZONA-COMUNA-4-CENTRO',
    'Comuna 4 - Centro Histórico',
    'Perímetro colonial delimitado entre el Parque Caldas, Puente del Humilladero y Santo Domingo.',
    '{"type":"Polygon","coordinates":[[[-76.608,2.441],[-76.603,2.441],[-76.603,2.447],[-76.608,2.447],[-76.608,2.441]]]}',
    2.444000,
    -76.605500,
    9,
    '8966c6c748fffff',
    7,
    TRUE,
    CURRENT_TIMESTAMP,
    'SISTEMA'
),
(
    'ZONA-COMUNA-2-NORTE',
    'Comuna 2 - Sector Norte y Campanario',
    'Zona comercial y residencial del norte que conecta Campanario, Villa Olímpica y Bella Vista.',
    '{"type":"Polygon","coordinates":[[[-76.598,2.455],[-76.590,2.455],[-76.590,2.468],[-76.598,2.468],[-76.598,2.455]]]}',
    2.461500,
    -76.594000,
    9,
    '8966c6d5bd3ffff',
    10,
    TRUE,
    CURRENT_TIMESTAMP,
    'SISTEMA'
),
(
    'ZONA-COMUNA-7-OCCIDENTE',
    'Comuna 7 - Sector Pandiguando y La Esmeralda',
    'Núcleo residencial tradicional del occidente payanés y nodo de abastos de La Esmeralda.',
    '{"type":"Polygon","coordinates":[[[-76.625,2.435],[-76.615,2.435],[-76.615,2.445],[-76.625,2.445],[-76.625,2.435]]]}',
    2.440000,
    -76.620000,
    9,
    '8966c6c745bffff',
    8,
    TRUE,
    CURRENT_TIMESTAMP,
    'SISTEMA'
);
