-- Descuentos por temporada, cupones, pagos simulados, favoritos y chat.
CREATE TABLE descuentos_temporada (
    id          uuid        NOT NULL,
    nombre      varchar(80) NOT NULL,
    producto_id uuid        NULL,
    porcentaje  int         NOT NULL,
    inicio      date        NOT NULL,
    fin         date        NOT NULL,
    activo      bit(1)      NOT NULL,
    CONSTRAINT pk_descuentos_temporada PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE cupones (
    id           uuid        NOT NULL,
    codigo       varchar(30) NOT NULL,
    comprador_id uuid        NOT NULL,
    porcentaje   int         NOT NULL,
    vence_el     date        NOT NULL,
    usado_en     datetime(6) NULL,
    CONSTRAINT pk_cupones PRIMARY KEY (id),
    CONSTRAINT uq_cupones_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE pagos (
    id                  uuid           NOT NULL,
    compra_id           uuid           NOT NULL,
    comprador_id        uuid           NOT NULL,
    metodo              varchar(10)    NOT NULL,
    ultimos_digitos     varchar(4)     NULL,
    referencia          varchar(40)    NOT NULL,
    subtotal            decimal(19, 2) NOT NULL,
    descuento_temporada decimal(19, 2) NOT NULL,
    descuento_cupon     decimal(19, 2) NOT NULL,
    total               decimal(19, 2) NOT NULL,
    cupon               varchar(30)    NULL,
    fecha               datetime(6)    NOT NULL,
    estado              varchar(15)    NOT NULL,
    CONSTRAINT pk_pagos PRIMARY KEY (id),
    CONSTRAINT uq_pagos_compra UNIQUE (compra_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE favoritos (
    id           uuid        NOT NULL,
    comprador_id uuid        NOT NULL,
    producto_id  uuid        NOT NULL,
    agregado     datetime(6) NOT NULL,
    CONSTRAINT pk_favoritos PRIMARY KEY (id),
    CONSTRAINT uq_favoritos UNIQUE (comprador_id, producto_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE conversaciones (
    id           uuid        NOT NULL,
    comprador_id uuid        NOT NULL,
    campesino_id uuid        NOT NULL,
    creada       datetime(6) NOT NULL,
    CONSTRAINT pk_conversaciones PRIMARY KEY (id),
    CONSTRAINT uq_conversaciones UNIQUE (comprador_id, campesino_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE mensajes (
    id              uuid          NOT NULL,
    conversacion_id uuid          NOT NULL,
    autor           uuid          NOT NULL,
    texto           varchar(1000) NOT NULL,
    momento         datetime(6)   NOT NULL,
    CONSTRAINT pk_mensajes PRIMARY KEY (id),
    CONSTRAINT fk_mensajes_conversacion FOREIGN KEY (conversacion_id) REFERENCES conversaciones (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_mensajes_conversacion ON mensajes (conversacion_id, momento);
