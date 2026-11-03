CREATE TABLE usuarios (
    id          uuid         NOT NULL,
    correo      varchar(120) NOT NULL,
    clave_hash  varchar(100) NOT NULL,
    rol         varchar(20)  NOT NULL,
    activo      bit(1)       NOT NULL,
    CONSTRAINT pk_usuarios PRIMARY KEY (id),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;