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
