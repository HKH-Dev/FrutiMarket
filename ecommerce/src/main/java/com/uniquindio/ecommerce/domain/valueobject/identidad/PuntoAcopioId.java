package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record PuntoAcopioId(UUID valor) {
    public PuntoAcopioId {
        Objects.requireNonNull(valor, "El identificador de un punto de acopio no puede ser nulo.");
    }
    public static PuntoAcopioId nuevo() {
        return new PuntoAcopioId(UUID.randomUUID());
    }

    public static PuntoAcopioId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un punto de acopio no puede ser nulo.");
        return new PuntoAcopioId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
