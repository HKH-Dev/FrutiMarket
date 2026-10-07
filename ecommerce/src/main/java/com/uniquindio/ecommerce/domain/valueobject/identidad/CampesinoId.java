package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record CampesinoId(UUID valor) {

    public CampesinoId {
        Objects.requireNonNull(valor, "El identificador de un campesino no puede ser nulo.");
    }

    public static CampesinoId nuevo() {
        return new CampesinoId(UUID.randomUUID());
    }

    public static CampesinoId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un campesino no puede ser nulo.");
        return new CampesinoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
