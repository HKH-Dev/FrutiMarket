package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class AlmacenId {

    private final UUID valor;

    private AlmacenId(UUID valor) {
        this.valor = valor;
    }

    public static AlmacenId nuevo() {
        return new AlmacenId(UUID.randomUUID());
    }

    public static AlmacenId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un punto de almacenamiento no puede ser nulo.");
        return new AlmacenId(valor);
    }

    public static AlmacenId de(String valor) {
        ReglaDeNegocioVioladaException.validar(valor != null && !valor.isBlank(), "INV-ID",
                "El identificador de un punto de almacenamiento no puede estar vacio.");
        return new AlmacenId(UUID.fromString(valor.trim()));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlmacenId otro && valor.equals(otro.valor);
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
