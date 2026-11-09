-- Catalogo de demostracion para MariaDB. Fechas relativas: los lotes no nacen vencidos.
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
 DATE_ADD(CURDATE(), INTERVAL -5 DAY), 'Cosecha manual en la finca La Esperanza, fruta madura de arbol', DATE_ADD(CURDATE(), INTERVAL -5 DAY), DATE_ADD(CURDATE(), INTERVAL 25 DAY),
 'PRIMERA', 5, FALSE,
 'KILOGRAMO', 500.000, 500.000, 3500, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 '2026-10-25 06:00:00', '2026-10-25 12:00:00', 1),

('66666666-0000-0000-0000-000000000002', 'LOT-MORA-001', '11111111-0000-0000-0000-000000000002', 'Mora de castilla',
 'MATERIA_PRIMA', 'FRUTA', 'REFRIGERADO', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'ORGANICA_CERTIFICADA',
 DATE_ADD(CURDATE(), INTERVAL -4 DAY), 'Mora organica certificada, cosechada al amanecer y enfriada en el acopio', DATE_ADD(CURDATE(), INTERVAL -4 DAY), DATE_ADD(CURDATE(), INTERVAL 6 DAY),
 'EXTRA', 3, TRUE,
 'KILOGRAMO', 120.000, 120.000, 9000, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', '44444444-0000-0000-0000-000000000001', FALSE,
 '2026-10-26 06:30:00', '2026-10-26 13:00:00', 1),

('66666666-0000-0000-0000-000000000003', 'LOT-PLATANO-001', '11111111-0000-0000-0000-000000000003', 'Platano harton',
 'MATERIA_PRIMA', 'FRUTA', 'AMBIENTE', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000001', 'TRADICIONAL',
 DATE_ADD(CURDATE(), INTERVAL -6 DAY), 'Racimos cortados verdes, para maduracion en punto de venta', DATE_ADD(CURDATE(), INTERVAL -6 DAY), DATE_ADD(CURDATE(), INTERVAL 9 DAY),
 'SEGUNDA', 8, FALSE,
 'KILOGRAMO', 300.000, 300.000, 1800, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 '2026-10-24 05:45:00', '2026-10-24 11:00:00', 1),

('66666666-0000-0000-0000-000000000004', 'LOT-CAFE-001', '11111111-0000-0000-0000-000000000004', 'Cafe tostado',
 'TRANSFORMADO', 'DERIVADO_PROCESADO', 'AMBIENTE', NULL, NULL,
 '22222222-0000-0000-0000-000000000001', '55555555-0000-0000-0000-000000000002', 'ORGANICA_CERTIFICADA',
 DATE_ADD(CURDATE(), INTERVAL -29 DAY), 'Cafe tostado medio, empacado en bolsa de valvula', DATE_ADD(CURDATE(), INTERVAL -29 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY),
 'EXTRA', 0, TRUE,
 'KILOGRAMO', 80.000, 80.000, 22000, 0.00,
 'Sin merma prevista', FALSE, 'PUBLICADO', NULL, FALSE,
 '2026-10-01 08:00:00', '2026-10-01 15:00:00', 1);

-- Cadena de custodia inicial: cada lote arranca con su registro de cosecha
INSERT INTO lote_custodia (lote_id, orden, tipo, almacen_id, guia_id, lugar, responsable, momento, temperatura_c, observacion) VALUES
('66666666-0000-0000-0000-000000000001', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', '2026-10-25 06:00:00', NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000002', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', '2026-10-26 06:30:00', NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000002', 1, 'INGRESO_ALMACEN', '44444444-0000-0000-0000-000000000001', NULL, 'Acopio Armenia', 'Operador de acopio', '2026-10-26 10:00:00', 5.00, 'Ingreso a cadena de frio'),
('66666666-0000-0000-0000-000000000003', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', '2026-10-24 05:45:00', NULL, 'Cosecha registrada por el campesino'),
('66666666-0000-0000-0000-000000000004', 0, 'COSECHA', NULL, NULL, 'Finca La Esperanza, Armenia', 'Maria Gomez', '2026-10-01 08:00:00', NULL, 'Cafe tostado y empacado en la finca');

-- Campana y cupon de demostracion, con fechas relativas para que siempre esten vigentes
INSERT INTO descuentos_temporada (id, nombre, producto_id, porcentaje, inicio, fin, activo) VALUES
('77777777-0000-0000-0000-000000000001', 'Cosecha de mango', '11111111-0000-0000-0000-000000000001', 15, DATE_ADD(CURDATE(), INTERVAL -5 DAY), DATE_ADD(CURDATE(), INTERVAL 45 DAY), TRUE);

INSERT INTO cupones (id, codigo, comprador_id, porcentaje, vence_el, usado_en) VALUES
('88888888-0000-0000-0000-000000000001', 'BIENVENIDA10', '33333333-0000-0000-0000-000000000001', 10, DATE_ADD(CURDATE(), INTERVAL 60 DAY), NULL);
