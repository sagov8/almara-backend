-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V2: INSERCIÓN DE CATÁLOGOS INICIALES
-- ==============================================================================

-- 1. Catálogo Oficial de Emociones Predeterminadas
INSERT INTO catalogo_emocion (id_emocion, nombre_etiqueta, codigo_hex_color, codigo_hex_fondo, icono_svg, descripcion, activo)
VALUES
    ('FELICIDAD', 'Felicidad', '#10B981', '#D1FAE5', '😊', 'Alegría, satisfacción y bienestar general.', TRUE),
    ('NEUTRALIDAD', 'Neutralidad', '#EAB308', '#FEF9C3', '😐', 'Estado equilibrado, tranquilo y regular.', TRUE),
    ('PREOCUPACION', 'Preocupación', '#F97316', '#FFEDD5', '😟', 'Inquietud o tensión moderada ante una situación.', TRUE),
    ('ENFADO', 'Enfado', '#EF4444', '#FEE2E2', '😠', 'Molestia, frustración o irritación.', TRUE),
    ('ANSIEDAD', 'Ansiedad', '#8B5CF6', '#EDE9FE', '😰', 'Nerviosismo, agobio o estrés acumulado.', TRUE);

-- 2. Catálogo Oficial de Zonas Urbanas de Referencia (Bogotá H3 Resolución 9)
INSERT INTO zona (id_celda_h3, resolucion_h3, latitud_centroide, longitud_centroide, zona_manual_id, nombre_zona_manual, umbral_minimo)
VALUES
    ('8966e42d20bffff', 9, 4.648600, -74.064500, 'ZONA-CHAPINERO', 'Chapinero Central', 5),
    ('8966e42f233ffff', 9, 4.697400, -74.029800, 'ZONA-USAQUEN', 'Usaquén', 5),
    ('8966e092043ffff', 9, 4.631500, -74.081700, 'ZONA-TEUSAQUILLO', 'Teusaquillo / Parkway', 5),
    ('8966e092e2bffff', 9, 4.598100, -74.075800, 'ZONA-CENTRO', 'La Candelaria / Centro Histórico', 5),
    ('8966e42f277ffff', 9, 4.743100, -74.088600, 'ZONA-SUBA', 'Suba Centro', 5),
    ('8966e0924abffff', 9, 4.653400, -74.108600, 'ZONA-SALITRE', 'Ciudad Salitre', 5);
