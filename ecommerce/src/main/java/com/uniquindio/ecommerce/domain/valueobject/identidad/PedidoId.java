package com.uniquindio.ecommerce.domain.valueobject.identidad;

import java.util.Objects;
import java.util.UUID;

public record PedidoId(UUID valor) {

    public PedidoId {
        Objects.requireNonNull(valor, "El identificador de un pedido no puede ser nulo.");
    }

    public static PedidoId nuevo() {
        return new PedidoId(UUID.randomUUID());
    }

    public static PedidoId de(String valor) {
        Objects.requireNonNull(valor, "El identificador de un pedido no puede ser nulo.");
        return new PedidoId(UUID.fromString(valor));
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
