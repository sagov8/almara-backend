-- Migración V4: Crear tabla estado_emocional_zona para agregación y visualización en mapa (HU-05)
-- Basado en almara_dbdiagram.dbml

CREATE TABLE IF NOT EXISTS estado_emocional_zona (
    id_celda_h3 VARCHAR(15) PRIMARY KEY,
    emocion_predominante VARCHAR(20),
    codigo_hex_color VARCHAR(7),
    icono_svg VARCHAR(255),
    distribucion_porcentual VARCHAR(1000),
    tendencia VARCHAR(50),
    participantes_aproximados VARCHAR(50),
    total_eventos_periodo INTEGER NOT NULL DEFAULT 0,
    periodo_temporal_metrica VARCHAR(50),
    cumple_umbral BOOLEAN NOT NULL DEFAULT FALSE,
    ultima_actualizacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_celda_h3) REFERENCES zona(id_celda_h3),
    FOREIGN KEY (emocion_predominante) REFERENCES catalogo_emocion(id_emocion)
);

CREATE INDEX IF NOT EXISTS idx_estado_zona_umbral ON estado_emocional_zona(cumple_umbral);
