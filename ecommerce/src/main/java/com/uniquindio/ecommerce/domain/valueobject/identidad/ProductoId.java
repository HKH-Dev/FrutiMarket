package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record ProductoId(UUID valor) {

    public ProductoId {
        Objects.requireNonNull(valor, "El identificador de un producto no puede ser nulo.");
    }

    public static ProductoId nuevo() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un producto no puede ser nulo.");
        return new ProductoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
