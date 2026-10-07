package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;


public record LoteId(UUID valor) {

    public LoteId {
        Objects.requireNonNull(valor, "El identificador de un lote no puede ser nulo.");
    }

    public static LoteId nuevo() {
        return new LoteId(UUID.randomUUID());
    }

    public static LoteId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un lote no puede ser nulo.");
        return new LoteId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
