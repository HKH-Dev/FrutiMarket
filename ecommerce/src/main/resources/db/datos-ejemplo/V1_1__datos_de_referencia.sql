-- Datos de referencia para empezar a usar MariaDB: catalogo, actores y un punto de acopio.
-- Son los mismos de data.sql (H2), pero SIN lotes ni compras: esos se crean por la API,
-- asi las fechas de cosecha y vencimiento siempre son reales.
-- Esta carpeta (db/datos-ejemplo) se puede quitar de spring.flyway.locations en produccion.

INSERT INTO productos (id, nombre, linea, tipo_cultivo, unidad_venta, temporada_inicio, temporada_fin, conservacion, vida_util_dias, activo) VALUES
('11111111-0000-0000-0000-000000000001', 'Mango Tommy',      'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'AMBIENTE',    30,  1),
('11111111-0000-0000-0000-000000000002', 'Mora de castilla', 'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'REFRIGERADO', 10,  1),
('11111111-0000-0000-0000-000000000003', 'Platano harton',   'MATERIA_PRIMA', 'FRUTA',              'KILOGRAMO', NULL, NULL, 'AMBIENTE',    15,  1),
('11111111-0000-0000-0000-000000000004', 'Cafe tostado',     'TRANSFORMADO',  'DERIVADO_PROCESADO', 'KILOGRAMO', NULL, NULL, 'AMBIENTE',    180, 1);

INSERT INTO campesinos (id, nombre, numero_identificacion, escala_produccion, autorizado_para_publicar) VALUES
('22222222-0000-0000-0000-000000000001', 'Maria Gomez',   '1094000001', 2.5, 1),
('22222222-0000-0000-0000-000000000002', 'Jorge Ramirez', '1094000002', 1.0, 0);

INSERT INTO compradores (id, nombre, tipo, municipio, activo) VALUES
('33333333-0000-0000-0000-000000000001', 'Restaurante El Solar', 'RESTAURANTE', 'Armenia', 1),
('33333333-0000-0000-0000-000000000002', 'Tienda Don Pedro',     'TIENDA',      'Calarca', 1);

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
