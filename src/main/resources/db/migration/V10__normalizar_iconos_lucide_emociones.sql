-- ==============================================================================
-- ALMARA CORE - MIGRACIÓN V10: NORMALIZACIÓN DE ÍCONOS VECTORIALES LUCIDE
-- ==============================================================================
UPDATE catalogo_emocion SET icono_svg = 'Smile' WHERE id_emocion = 'FELICIDAD';
UPDATE catalogo_emocion SET icono_svg = 'Meh' WHERE id_emocion = 'NEUTRALIDAD';
UPDATE catalogo_emocion SET icono_svg = 'Frown' WHERE id_emocion = 'PREOCUPACION';
UPDATE catalogo_emocion SET icono_svg = 'Flame' WHERE id_emocion = 'ENFADO';
UPDATE catalogo_emocion SET icono_svg = 'CloudRain' WHERE id_emocion = 'ANSIEDAD';
