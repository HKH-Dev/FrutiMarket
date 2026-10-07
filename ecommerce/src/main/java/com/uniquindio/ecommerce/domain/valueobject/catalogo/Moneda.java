package com.uniquindio.ecommerce.domain.valueobject.catalogo;

/** Monedas admitidas por el marketplace. Enum: conjunto cerrado. */
public enum Moneda {
    COP("$", 0),
    USD("US$", 2);

    private final String simbolo;
    private final int escala;

    Moneda(String simbolo, int escala) {
        this.simbolo = simbolo;
        this.escala = escala;
    }

    public String simbolo() {
        return simbolo;
    }

    public int escala() {
        return escala;
    }
}
