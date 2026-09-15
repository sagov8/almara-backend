-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V1: DEFINICIÓN DE TABLAS BASE SEGÚN DBML
-- ==============================================================================

-- 1. Tabla de Catálogo de Emociones (HU-01, HU-12)
CREATE TABLE IF NOT EXISTS catalogo_emocion (
    id_emocion VARCHAR(20) PRIMARY KEY,
    nombre_etiqueta VARCHAR(30) NOT NULL,
    codigo_hex_color VARCHAR(7) NOT NULL,
    codigo_hex_fondo VARCHAR(7),
    icono_svg VARCHAR(255),
    descripcion VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabla de Zonas y Celdas H3 (HU-04, HU-13, HU-14)
CREATE TABLE IF NOT EXISTS zona (
    id_celda_h3 VARCHAR(15) PRIMARY KEY,
    resolucion_h3 INT NOT NULL,
    latitud_centroide DECIMAL(10, 6) NOT NULL,
    longitud_centroide DECIMAL(10, 6) NOT NULL,
    zona_manual_id VARCHAR(50) UNIQUE,
    nombre_zona_manual VARCHAR(100),
    umbral_minimo INT NOT NULL DEFAULT 5
);

-- 3. Tabla de Reportes de Emociones Individuales (Anónimas) (HU-01, HU-02, HU-03)
CREATE TABLE IF NOT EXISTS reporte_emocion (
    id_evento UUID PRIMARY KEY,
    id_celda_h3 VARCHAR(15) NOT NULL REFERENCES zona(id_celda_h3),
    id_emocion VARCHAR(20) NOT NULL REFERENCES catalogo_emocion(id_emocion),
    intensidad FLOAT,
    comentario VARCHAR(200),
    token_sesion_temporal VARCHAR(64) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimización de agregaciones geoespaciales y control temporal
CREATE INDEX IF NOT EXISTS idx_reporte_celda_h3 ON reporte_emocion(id_celda_h3);
CREATE INDEX IF NOT EXISTS idx_reporte_emocion ON reporte_emocion(id_emocion);
CREATE INDEX IF NOT EXISTS idx_reporte_fecha_hora ON reporte_emocion(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_zona_manual_id ON zona(zona_manual_id);
