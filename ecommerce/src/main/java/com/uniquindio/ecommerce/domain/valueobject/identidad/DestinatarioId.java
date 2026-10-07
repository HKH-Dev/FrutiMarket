package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record DestinatarioId(UUID valor) {

    public DestinatarioId {
        Objects.requireNonNull(valor, "El identificador de un destinatario no puede ser nulo.");
    }

    public static DestinatarioId nuevo() {
        return new DestinatarioId(UUID.randomUUID());
    }

    public static DestinatarioId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un destinatario no puede ser nulo.");
        return new DestinatarioId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
