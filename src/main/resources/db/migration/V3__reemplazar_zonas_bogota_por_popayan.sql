-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V3: REEMPLAZAR ZONAS POR LAS 15 UBICACIONES DE POPAYÁN
-- ==============================================================================

-- 1. Agregar columna de descripción detallada a la tabla zona si no existe
ALTER TABLE zona ADD COLUMN IF NOT EXISTS descripcion VARCHAR(255);

-- 2. Eliminar las zonas anteriores de Bogotá
DELETE FROM zona WHERE zona_manual_id IN (
    'ZONA-CHAPINERO', 'ZONA-USAQUEN', 'ZONA-TEUSAQUILLO',
    'ZONA-CENTRO', 'ZONA-SUBA', 'ZONA-SALITRE'
);

-- 3. Inserción de las 15 ubicaciones emblemáticas de Popayán (H3 Resolución 9)
INSERT INTO zona (id_celda_h3, resolucion_h3, latitud_centroide, longitud_centroide, zona_manual_id, nombre_zona_manual, descripcion, umbral_minimo)
VALUES
    ('8966c6c748fffff', 9, 2.443431, -76.605638, 'ZONA-PARQUE-CALDAS', 'Parque Caldas', 'El eje fundacional, rodeado por la Catedral Basílica, Torre del Reloj y sedes de gobierno.', 5),
    ('8966c6c7483ffff', 9, 2.446669, -76.605328, 'ZONA-HUMILLADERO', 'Puente del Humilladero', 'Estructura de once arcos de ladrillo y calicanto sobre la depresión del río Molino.', 5),
    ('8966c6d5b4bffff', 9, 2.445989, -76.599611, 'ZONA-MORRO-TULCAN', 'El Morro de Tulcán', 'Sitio arqueológico prehispánico y mirador natural emblemático de Popayán.', 5),
    ('8966c6c74a3ffff', 9, 2.439513, -76.600229, 'ZONA-ERMITA', 'Ermita de Jesús Nazareno', 'La iglesia más antigua en pie de la ciudad (siglo XVI) sobre colina con vista al centro.', 5),
    ('8966c6c7413ffff', 9, 2.442153, -76.608651, 'ZONA-PANTEON-PROCERES', 'Panteón de los Próceres', 'Antiguo templo de Santo Domingo reconvertido en mausoleo de próceres y expresidentes.', 5),
    ('8966c6d5d8bffff', 9, 2.485945, -76.563997, 'ZONA-TERRA-PLAZA', 'Centro Comercial Terra Plaza', 'Principal nodo de comercio moderno, servicios y entretenimiento del sector norte.', 5),
    ('8966c6c7497ffff', 9, 2.447948, -76.602315, 'ZONA-CAMPUS-TULCAN', 'Campus Tulcán (Unicauca)', 'Centro neurálgico universitario y académico público entre el centro y el norte.', 5),
    ('8966c6d5aafffff', 9, 2.458939, -76.598374, 'ZONA-BELLA-VISTA', 'Sector de Bella Vista', 'Área residencial y comercial contemporánea, referente de salud e institutos técnicos.', 5),
    ('8966c6d587bffff', 9, 2.464733, -76.592039, 'ZONA-VILLA-OLIMPICA', 'Villa Olímpica', 'Complejo deportivo central para atletismo, natación y fútbol sobre la Carrera 6.', 5),
    ('8966c6c7487ffff', 9, 2.444710, -76.602624, 'ZONA-RINCON-PAYANES', 'Rincón Payanés (Pueblito Patojo)', 'Réplica a escala de monumentos coloniales rodeada de zonas verdes y gastronomía.', 5),
    ('8966c6d5b07ffff', 9, 2.445906, -76.585162, 'ZONA-YANACONAS', 'Sector Calicanto / Yanaconas', 'Enclave periurbano tradicional con arquitectura colonial rural e iglesia de San José.', 5),
    ('8966c6c749bffff', 9, 2.448628, -76.608033, 'ZONA-PARQUE-SALUD', 'Parque de la Salud (Los Hoyos)', 'Espacio recreativo y sendero ecológico para actividad física matutina al aire libre.', 5),
    ('8966c6c745bffff', 9, 2.440275, -76.620397, 'ZONA-LA-ESMERALDA', 'Galería La Esmeralda', 'Centro de abastos y distribución alimentaria más activo del sur de Popayán.', 5),
    ('8966c6c7407ffff', 9, 2.436955, -76.606256, 'ZONA-EL-EMPEDRADO', 'Sector El Empedrado', 'Barrio tradicional del sur del centro con calles empinadas de piedra y talleres artesanales.', 5),
    ('8966c6d5bd3ffff', 9, 2.458258, -76.592657, 'ZONA-CAMPANARIO', 'Centro Comercial Campanario', 'Punto bisagra entre el centro y norte, nodo de encuentro social y financiero de mayor flujo.', 5);
