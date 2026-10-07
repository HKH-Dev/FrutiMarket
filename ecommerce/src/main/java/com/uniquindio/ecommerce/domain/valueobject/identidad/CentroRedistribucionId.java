package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record CentroRedistribucionId(UUID valor) {

    public CentroRedistribucionId {
        Objects.requireNonNull(valor, "El identificador de un centro de redistribucion por cultivo no puede ser nulo.");
    }

    public static CentroRedistribucionId nuevo() {
        return new CentroRedistribucionId(UUID.randomUUID());
    }

    public static CentroRedistribucionId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un centro de redistribucion por cultivo no puede ser nulo.");
        return new CentroRedistribucionId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
