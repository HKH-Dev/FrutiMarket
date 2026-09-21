package com.uniquindio.ecommerce.domain.valueobject;

public enum UnidadMedida {
    KILOGRAMO("kg"), LIBRA("lb"), TONELADA("t"), UNIDAD("und"), CANASTILLA("can");

    private final String simbolo;

    UnidadMedida(String simbolo) { this.simbolo = simbolo; }

    public String getSimbolo() { return simbolo; }
}