-- =====================================================================
-- V1: esquema inicial de FrutiMarket para MariaDB 10.7 o superior.
--
-- Flyway ejecuta este archivo UNA sola vez y lo anota en la tabla
-- flyway_schema_history. Nunca se edita despues de aplicado: los cambios
-- futuros van en un archivo nuevo (V2__..., V3__...).
--
-- Las columnas son las mismas que esperan las entidades JPA
-- (infraestructure/persistence/jpa/entity). Los enums de Java se guardan
-- como VARCHAR (no como ENUM de MariaDB) para que agregar un valor en el
-- codigo no obligue a modificar la tabla.
-- =====================================================================

-- ---------- Catalogo ----------
CREATE TABLE productos (
    id                uuid         NOT NULL,
    nombre            varchar(120) NOT NULL,
    linea             varchar(20)  NOT NULL,
    tipo_cultivo      varchar(30)  NOT NULL,
    unidad_venta      varchar(20)  NOT NULL,
    temporada_inicio  varchar(7)   NULL,      -- formato MonthDay "--MM-dd"; NULL = todo el anio
    temporada_fin     varchar(7)   NULL,
    conservacion      varchar(20)  NOT NULL,
    vida_util_dias    int          NOT NULL,
    activo            bit(1)       NOT NULL,
    CONSTRAINT pk_productos PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------- Actores ----------
CREATE TABLE campesinos (
    id                        uuid         NOT NULL,
    nombre                    varchar(120) NOT NULL,
    numero_identificacion     varchar(30)  NOT NULL,
    escala_produccion         double       NOT NULL,
    autorizado_para_publicar  bit(1)       NOT NULL,
    CONSTRAINT pk_campesinos PRIMARY KEY (id),
    CONSTRAINT uk_campesinos_identificacion UNIQUE (numero_identificacion)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE compradores (
    id         uuid         NOT NULL,
    nombre     varchar(120) NOT NULL,
    tipo       varchar(20)  NOT NULL,
    municipio  varchar(80)  NOT NULL,
    activo     bit(1)       NOT NULL,
    CONSTRAINT pk_compradores PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------- Logistica ----------
CREATE TABLE puntos_almacenamiento (
    id               uuid         NOT NULL,
    nombre           varchar(120) NOT NULL,
    tipo             varchar(30)  NOT NULL,
    municipio        varchar(80)  NOT NULL,
    capacidad_lotes  int          NOT NULL,
    CONSTRAINT pk_puntos_almacenamiento PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE almacen_municipios_cobertura (
    almacen_id  uuid        NOT NULL,
    municipio   varchar(80) NOT NULL,   -- normalizado: minusculas y sin espacios extremos
    CONSTRAINT pk_almacen_municipios PRIMARY KEY (almacen_id, municipio),
    CONSTRAINT fk_almacen_municipios_almacen FOREIGN KEY (almacen_id) REFERENCES puntos_almacenamiento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE almacen_cultivos_habilitados (
    almacen_id    uuid        NOT NULL,
    tipo_cultivo  varchar(30) NOT NULL,
    CONSTRAINT pk_almacen_cultivos PRIMARY KEY (almacen_id, tipo_cultivo),
    CONSTRAINT fk_almacen_cultivos_almacen FOREIGN KEY (almacen_id) REFERENCES puntos_almacenamiento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE almacen_conservaciones (
    almacen_id    uuid        NOT NULL,
    conservacion  varchar(20) NOT NULL,
    CONSTRAINT pk_almacen_conservaciones PRIMARY KEY (almacen_id, conservacion),
    CONSTRAINT fk_almacen_conservaciones_almacen FOREIGN KEY (almacen_id) REFERENCES puntos_almacenamiento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE almacen_lotes (
    almacen_id  uuid NOT NULL,
    lote_id     uuid NOT NULL,
    CONSTRAINT pk_almacen_lotes PRIMARY KEY (almacen_id, lote_id),
    CONSTRAINT fk_almacen_lotes_almacen FOREIGN KEY (almacen_id) REFERENCES puntos_almacenamiento (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------- Agregado Lote ----------
-- Los datos del producto se copian al registrar el lote (igual que en el dominio),
-- por eso no hay llave foranea hacia productos.
CREATE TABLE lotes (
    id                            uuid          NOT NULL,
    codigo                        varchar(60)   NOT NULL,
    -- producto (copia al registrar)
    producto_id                   uuid          NOT NULL,
    nombre_producto               varchar(120)  NOT NULL,
    linea                         varchar(20)   NOT NULL,
    tipo_cultivo                  varchar(30)   NOT NULL,
    conservacion                  varchar(20)   NOT NULL,
    temporada_inicio              varchar(7)    NULL,
    temporada_fin                 varchar(7)    NULL,
    -- origen y trazabilidad (regla 8)
    campesino_id                  uuid          NOT NULL,
    finca_origen                  uuid          NOT NULL,
    tecnica_produccion            varchar(30)   NOT NULL,
    ficha_fecha_cosecha           date          NOT NULL,
    ficha_observaciones           varchar(500)  NOT NULL,
    fecha_cosecha                 date          NOT NULL,
    fecha_limite_consumo          date          NULL,
    -- calidad (regla 18)
    calidad_declarada_categoria   varchar(20)   NOT NULL,
    calidad_declarada_defectos    int           NOT NULL,
    calidad_declarada_organico    bit(1)        NOT NULL,
    calidad_verificada_categoria  varchar(20)   NULL,
    calidad_verificada_defectos   int           NULL,
    calidad_verificada_organico   bit(1)        NULL,
    -- cantidades y precio (reglas 2 y 3), todas en la misma unidad
    unidad                        varchar(20)   NOT NULL,
    cantidad_inicial              decimal(19,3) NOT NULL,
    cantidad_disponible           decimal(19,3) NOT NULL,
    precio_finca                  decimal(19,0) NULL,
    merma_porcentaje              decimal(5,2)  NOT NULL,
    merma_justificacion           varchar(300)  NOT NULL,
    merma_aplicada                bit(1)        NOT NULL,
    -- estado y custodia
    estado                        varchar(20)   NOT NULL,
    estado_previo_revision        varchar(20)   NULL,
    motivo_revision               varchar(30)   NULL,
    detalle_revision              varchar(500)  NULL,
    almacen_actual_id             uuid          NULL,
    cadena_frio_rota              bit(1)        NOT NULL,
    fecha_registro                datetime(6)   NOT NULL,
    fecha_publicacion             datetime(6)   NULL,
    version                       bigint        NOT NULL,
    CONSTRAINT pk_lotes PRIMARY KEY (id),
    CONSTRAINT uk_lotes_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_lotes_producto_estado ON lotes (producto_id, estado);
CREATE INDEX ix_lotes_campesino ON lotes (campesino_id);
CREATE INDEX ix_lotes_almacen ON lotes (almacen_actual_id);

CREATE TABLE lote_reservas (
    lote_id    uuid          NOT NULL,
    pedido_id  uuid          NOT NULL,
    cantidad   decimal(19,3) NOT NULL,
    momento    datetime(6)   NOT NULL,
    CONSTRAINT fk_lote_reservas_lote FOREIGN KEY (lote_id) REFERENCES lotes (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Cadena de custodia: "orden" conserva la secuencia cronologica (@OrderColumn).
CREATE TABLE lote_custodia (
    lote_id        uuid          NOT NULL,
    orden          int           NOT NULL,
    tipo           varchar(30)   NOT NULL,
    almacen_id     uuid          NULL,
    guia_id        uuid          NULL,
    lugar          varchar(200)  NOT NULL,
    responsable    varchar(120)  NOT NULL,
    momento        datetime(6)   NOT NULL,
    temperatura_c  decimal(6,2)  NULL,
    observacion    varchar(500)  NOT NULL,
    CONSTRAINT pk_lote_custodia PRIMARY KEY (lote_id, orden),
    CONSTRAINT fk_lote_custodia_lote FOREIGN KEY (lote_id) REFERENCES lotes (id),
    CONSTRAINT ck_lote_custodia_orden CHECK (orden >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------- Agregado Compra ----------
CREATE TABLE compras (
    id            uuid        NOT NULL,
    comprador_id  uuid        NOT NULL,
    fecha         datetime(6) NOT NULL,
    estado        varchar(20) NOT NULL,
    CONSTRAINT pk_compras PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_compras_comprador ON compras (comprador_id);

CREATE TABLE detalles_compra (
    compra_id        uuid          NOT NULL,
    detalle_id       uuid          NOT NULL,
    lote_id          uuid          NOT NULL,
    cantidad         decimal(19,3) NOT NULL,
    unidad           varchar(20)   NOT NULL,
    precio_unitario  decimal(19,0) NOT NULL,
    CONSTRAINT fk_detalles_compra_compra FOREIGN KEY (compra_id) REFERENCES compras (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
