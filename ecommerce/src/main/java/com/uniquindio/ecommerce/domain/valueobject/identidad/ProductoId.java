package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class ProductoId {

    private final UUID valor;

    private ProductoId(UUID valor) {
        this.valor = valor;
    }

    public static ProductoId nuevo() {
        return new ProductoId(UUID.randomUUID());
    }

    public static ProductoId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un producto no puede ser nulo.");
        return new ProductoId(valor);
    }

    public static ProductoId de(String valor) {
        ReglaDeNegocioVioladaException.validar(valor != null && !valor.isBlank(), "INV-ID",
                "El identificador de un producto no puede estar vacio.");
        return new ProductoId(UUID.fromString(valor.trim()));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProductoId otro && valor.equals(otro.valor);
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
