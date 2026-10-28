-- Datos de ejemplo para H2 (perfil por defecto). Se cargan despues de que Hibernate crea las tablas.
-- En el perfil "mariadb" no se ejecuta (spring.sql.init.mode=never).
-- Los UUID son fijos para poder usarlos directamente en Swagger.

-- Productos del catalogo
INSERT INTO productos (id, nombre, linea, tipo_cultivo, unidad_venta, temporada_inicio, temporada_fin, conservacion, vida_util_dias, activo) VALUES
('11111111-0000-0000-0000-000000000001', 'Mango Tommy',       'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'AMBIENTE',    30,  TRUE),
('11111111-0000-0000-0000-000000000002', 'Mora de castilla',  'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'REFRIGERADO', 10,  TRUE),
('11111111-0000-0000-0000-000000000003', 'Platano harton',    'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'AMBIENTE',    15,  TRUE),
('11111111-0000-0000-0000-000000000004', 'Cafe tostado',      'TRANSFORMADO',  'DERIVADO_PROCESADO', 'KILOGRAMO', NULL, NULL, 'AMBIENTE',    180, TRUE);

-- Campesinos (el segundo aun no esta autorizado para publicar)
INSERT INTO campesinos (id, nombre, numero_identificacion, escala_produccion, autorizado_para_publicar) VALUES
('22222222-0000-0000-0000-000000000001', 'Maria Gomez',   '1094000001', 2.5, TRUE),
('22222222-0000-0000-0000-000000000002', 'Jorge Ramirez', '1094000002', 1.0, FALSE);

-- Compradores
INSERT INTO compradores (id, nombre, tipo, municipio, activo) VALUES
('33333333-0000-0000-0000-000000000001', 'Restaurante El Solar', 'RESTAURANTE', 'Armenia',  TRUE),
('33333333-0000-0000-0000-000000000002', 'Tienda Don Pedro',     'TIENDA',      'Calarca',  TRUE);

-- Punto de acopio en Armenia que cubre tambien Calarca y Circasia
INSERT INTO puntos_almacenamiento (id, nombre, tipo, municipio, capacidad_lotes) VALUES
('44444444-0000-0000-0000-000000000001', 'Acopio Armenia', 'PUNTO_ACOPIO', 'Armenia', 50);

INSERT INTO almacen_municipios_cobertura (almacen_id, municipio) VALUES
('44444444-0000-0000-0000-000000000001', 'armenia'),
('44444444-0000-0000-0000-000000000001', 'calarca'),
('44444444-0000-0000-0000-000000000001', 'circasia');

INSERT INTO almacen_cultivos_habilitados (almacen_id, tipo_cultivo) VALUES
('44444444-0000-0000-0000-000000000001', 'FRUTA'),
('44444444-0000-0000-0000-000000000001', 'DERIVADO_PROCESADO');

INSERT INTO almacen_conservaciones (almacen_id, conservacion) VALUES
('44444444-0000-0000-0000-000000000001', 'AMBIENTE'),
('44444444-0000-0000-0000-000000000001', 'REFRIGERADO');

-- Lotes publicados (disponibles en el catalogo) del campesino autorizado Maria Gomez.
-- Sin merma prevista (0%), asi que la cantidad disponible es la inicial.
INSERT INTO lotes (id, codigo, producto_id, nombre_producto, linea, tipo_cultivo, conservacion,
                   temporada_inicio, temporada_fin, campesino_id, finca_origen, tecnica_produccion,
                   ficha_fecha_cosecha, ficha_observaciones, fecha_cosecha, fecha_limite_consumo,
                   calidad_declarada_categoria, calidad_declarada_defectos, calidad_declarada_organico,
                   unidad, cantidad_inicial, cantidad_disponible, precio_finca, merma_porcentaje,
                   merma_justificacion, merma_aplicada, estado, almacen_actual_id, cadena_frio_rota,
                   fecha_registro, fecha_publicacion, version) VALUES
('66666666-0000-0000-0000-000000000001', 'LOT-MANGO-001', '11111111-0000-0000-0000-000000000001', 'Mango Tommy',
 'MATERIA_PRIMA', 'FRUTA', 'AMBIENTE', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000001', 'AGROECOLOGICA',
 DATE '2026-10-25', 'Cosecha manual en la finca La Esperanza, fruta madura de arbol', DATE '2026-10-25', DATE '2026-11-24',
 'PRIMERA', 5, FALSE,
 'KILOGRAMO', 500.000, 500.000, 3500, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 CAST('2026-10-25T06:00:00Z' AS TIMESTAMP WITH TIME ZONE), CAST('2026-10-25T12:00:00Z' AS TIMESTAMP WITH TIME ZONE), 1),

('66666666-0000-0000-0000-000000000002', 'LOT-MORA-001', '11111111-0000-0000-0000-000000000002', 'Mora de castilla',
 'MATERIA_PRIMA', 'FRUTA', 'REFRIGERADO', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'ORGANICA_CERTIFICADA',
 DATE '2026-10-26', 'Mora organica certificada, cosechada al amanecer y enfriada en el acopio', DATE '2026-10-26', DATE '2026-11-05',
 'EXTRA', 3, TRUE,
 'KILOGRAMO', 120.000, 120.000, 9000, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', '44444444-0000-0000-0000-000000000001', FALSE,
 CAST('2026-10-26T06:30:00Z' AS TIMESTAMP WITH TIME ZONE), CAST('2026-10-26T13:00:00Z' AS TIMESTAMP WITH TIME ZONE), 1),

('66666666-0000-0000-0000-000000000003', 'LOT-PLATANO-001', '11111111-0000-0000-0000-000000000003', 'Platano harton',
 'MATERIA_PRIMA', 'FRUTA', 'AMBIENTE', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000001', 'TRADICIONAL',
 DATE '2026-10-24', 'Racimos cortados verdes, para maduracion en punto de venta', DATE '2026-10-24', DATE '2026-11-08',
 'SEGUNDA', 8, FALSE,
 'KILOGRAMO', 300.000, 300.000, 1800, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 CAST('2026-10-24T05:45:00Z' AS TIMESTAMP WITH TIME ZONE), CAST('2026-10-24T11:00:00Z' AS TIMESTAMP WITH TIME ZONE), 1),

('66666666-0000-0000-0000-000000000004', 'LOT-CAFE-001', '11111111-0000-0000-0000-000000000004', 'Cafe tostado',
 'TRANSFORMADO', 'DERIVADO_PROCESADO', 'AMBIENTE', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'ORGANICA_CERTIFICADA',
 DATE '2026-10-01', 'Cafe tostado medio, empacado en bolsa de valvula', DATE '2026-10-01', DATE '2027-03-29',
 'EXTRA', 0, TRUE,
 'KILOGRAMO', 80.000, 80.000, 22000, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 CAST('2026-10-01T08:00:00Z' AS TIMESTAMP WITH TIME ZONE), CAST('2026-10-01T15:00:00Z' AS TIMESTAMP WITH TIME ZONE), 1);

-- Cadena de custodia inicial: cada lote arranca con su registro de cosecha
INSERT INTO lote_custodia (lote_id, orden, tipo, almacen_id, guia_id, lugar, responsable, momento, temperatura_c, observacion) VALUES
('66666666-0000-0000-0000-000000000001', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', CAST('2026-10-25T06:00:00Z' AS TIMESTAMP WITH TIME ZONE), NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000002', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', CAST('2026-10-26T06:30:00Z' AS TIMESTAMP WITH TIME ZONE), NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000002', 1, 'INGRESO_ALMACEN', '44444444-0000-0000-0000-000000000001', NULL, 'Acopio Armenia', 'Operador de acopio', CAST('2026-10-26T10:00:00Z' AS TIMESTAMP WITH TIME ZONE), 5.00, 'Ingreso a cadena de frio'),
('66666666-0000-0000-0000-000000000003', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', CAST('2026-10-24T05:45:00Z' AS TIMESTAMP WITH TIME ZONE), NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000004', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', CAST('2026-10-01T08:00:00Z' AS TIMESTAMP WITH TIME ZONE), NULL, 'Cafe tostado y empacado en la finca');
