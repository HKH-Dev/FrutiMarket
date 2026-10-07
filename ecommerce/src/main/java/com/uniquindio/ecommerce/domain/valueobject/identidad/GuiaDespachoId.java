package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record GuiaDespachoId(UUID valor) {

    public GuiaDespachoId {
        Objects.requireNonNull(valor, "El identificador de una guia de despacho no puede ser nulo.");
    }

    public static GuiaDespachoId nuevo() {
        return new GuiaDespachoId(UUID.randomUUID());
    }

   public static GuiaDespachoId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de una guia de despacho no puede ser nulo.");
        return new GuiaDespachoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
