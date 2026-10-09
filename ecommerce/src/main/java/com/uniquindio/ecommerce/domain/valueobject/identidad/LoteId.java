package com.uniquindio.ecommerce.domain.valueobject.identidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.UUID;

public final class LoteId {

    private final UUID valor;

    private LoteId(UUID valor) {
        this.valor = valor;
    }

    public static LoteId nuevo() {
        return new LoteId(UUID.randomUUID());
    }

    public static LoteId de(UUID valor) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-ID", "El identificador de un lote no puede ser nulo.");
        return new LoteId(valor);
    }

    public static LoteId de(String valor) {
        ReglaDeNegocioVioladaException.validar(valor != null && !valor.isBlank(), "INV-ID",
                "El identificador de un lote no puede estar vacio.");
        return new LoteId(UUID.fromString(valor.trim()));
    }

    public UUID valor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LoteId otro && valor.equals(otro.valor);
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
