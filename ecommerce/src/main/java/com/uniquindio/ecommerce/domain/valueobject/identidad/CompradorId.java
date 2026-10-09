package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class CompradorId {

    private final UUID valor;

    private CompradorId(UUID valor) {
        this.valor = valor;
    }

    public static CompradorId nuevo() {
        return new CompradorId(UUID.randomUUID());
    }

    public static CompradorId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un comprador no puede ser nulo.");
        return new CompradorId(valor);
    }

    public static CompradorId de(String valor) {
        ReglaDeNegocioVioladaException.validar(valor != null && !valor.isBlank(), "INV-ID",
                "El identificador de un comprador no puede estar vacio.");
        return new CompradorId(UUID.fromString(valor.trim()));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CompradorId otro && valor.equals(otro.valor);
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
