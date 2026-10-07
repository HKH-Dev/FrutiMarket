package com.uniquindio.ecommerce.domain.valueobject.catalogo;

/**
 * Unidad en la que se mide un lote.
 *
 * <p>Es un <b>enum</b> y no un record porque el conjunto de unidades es cerrado,
 * finito y conocido en tiempo de compilacion: no se puede "crear" una unidad de
 * medida nueva en ejecucion. Cada constante lleva ademas los datos que el dominio
 * necesita para operar con ella (escala decimal y si admite fracciones).</p>
 */
public enum UnidadMedida {

    KILOGRAMO("kg", 3, true),
    GRAMO("g", 0, true),
    TONELADA("t", 3, true),
    LIBRA("lb", 2, true),
    ARROBA("@", 2, true),
    LITRO("L", 3, true),
    UNIDAD("und", 0, false),
    CANASTILLA("canastilla", 0, false),
    BULTO("bulto", 0, false);

    private final String simbolo;
    private final int escala;
    private final boolean admiteFraccion;

    UnidadMedida(String simbolo, int escala, boolean admiteFraccion) {
        this.simbolo = simbolo;
        this.escala = escala;
        this.admiteFraccion = admiteFraccion;
    }

    public String simbolo() {
        return simbolo;
    }

    /** Numero de decimales con los que se normaliza toda cantidad expresada en esta unidad. */
    public int escala() {
        return escala;
    }

    /** {@code false} para unidades discretas: no existen 2,5 canastillas. */
    public boolean admiteFraccion() {
        return admiteFraccion;
    }

    public boolean esDePeso() {
        return this == KILOGRAMO || this == GRAMO || this == TONELADA
                || this == LIBRA || this == ARROBA;
    }
}
