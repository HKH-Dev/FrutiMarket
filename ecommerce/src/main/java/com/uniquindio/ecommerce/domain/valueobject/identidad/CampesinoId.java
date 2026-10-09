package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class CampesinoId {

    private final UUID valor;

    private CampesinoId(UUID valor) {
        this.valor = valor;
    }

    public static CampesinoId nuevo() {
        return new CampesinoId(UUID.randomUUID());
    }

    public static CampesinoId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un campesino no puede ser nulo.");
        return new CampesinoId(valor);
    }

    public static CampesinoId de(String valor) {
        ReglaDeNegocioVioladaException.validar(valor != null && !valor.isBlank(), "INV-ID",
                "El identificador de un campesino no puede estar vacio.");
        return new CampesinoId(UUID.fromString(valor.trim()));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CampesinoId otro && valor.equals(otro.valor);
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
