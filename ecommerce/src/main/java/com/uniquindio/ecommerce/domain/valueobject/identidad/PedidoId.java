package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class PedidoId {

    private final UUID valor;

    private PedidoId(UUID valor) {
        this.valor = valor;
    }

    public static PedidoId nuevo() {
        return new PedidoId(UUID.randomUUID());
    }

    public static PedidoId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un pedido no puede ser nulo.");
        return new PedidoId(valor);
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PedidoId otro && valor.equals(otro.valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
