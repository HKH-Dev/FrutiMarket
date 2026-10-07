package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record FincaOrigenId(UUID valor) {

    public FincaOrigenId {
        Objects.requireNonNull(valor, "El identificador de una finca de origen no puede ser nulo.");
    }

   public static FincaOrigenId nuevo() {
        return new FincaOrigenId(UUID.randomUUID());
    }

    public static FincaOrigenId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de una finca de origen no puede ser nulo.");
        return new FincaOrigenId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
