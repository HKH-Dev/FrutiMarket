package com.uniquindio.ecommerce.domain.valueobject.catalogo;

public enum CategoriaCalibre {
    EXTRA("Extra", 1),
    PRIMERA("Primera", 2),
    SEGUNDA("Segunda", 3),
    TERCERA("Tercera", 4),
    INDUSTRIAL("Industrial", 5);

    private final String etiqueta;
    private final int orden;

    CategoriaCalibre(String etiqueta, int orden) {
        this.etiqueta = etiqueta;
        this.orden = orden;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Menor numero = mejor calidad comercial. */
    public int orden() {
        return orden;
    }

    public boolean esMejorQue(CategoriaCalibre otra) {
        return this.orden < otra.orden;
    }
}
