package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

/**
 * Tipo de cultivo del lote.
 *
 * <p><b>Enum</b>. Es el criterio con el que un Centro de Redistribucion decide si
 * puede recibir un lote (regla 13) y con el que se agrupan los lotes para aplicar
 * la rotacion por vida util (regla 16).</p>
 */
public enum TipoCultivo {
    FRUTA("Fruta", true),
    HORTALIZA_HOJA("Hortaliza de hoja", true),
    HORTALIZA_RAIZ("Hortaliza de raiz", true),
    TUBERCULO("Tuberculo", true),
    LEGUMINOSA("Leguminosa", false),
    CEREAL("Cereal", false),
    AROMATICA("Aromatica", true),
    DERIVADO_PROCESADO("Derivado procesado", false);

    private final String etiqueta;
    private final boolean perecederoPorNaturaleza;

    TipoCultivo(String etiqueta, boolean perecederoPorNaturaleza) {
        this.etiqueta = etiqueta;
        this.perecederoPorNaturaleza = perecederoPorNaturaleza;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Indica si el cultivo es perecedero salvo que un proceso de transformacion lo estabilice. */
    public boolean esPerecederoPorNaturaleza() {
        return perecederoPorNaturaleza;
    }
}
