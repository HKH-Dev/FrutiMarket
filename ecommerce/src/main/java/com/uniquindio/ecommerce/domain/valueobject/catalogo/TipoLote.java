package com.uniquindio.ecommerce.domain.valueobject.catalogo;

/**
 * Linea de catalogo a la que pertenece el lote.
 *
 * <p><b>Enum</b>: el negocio define exactamente dos lineas y de ellas depende que
 * datos son obligatorios (un lote transformado exige ficha de transformacion; uno
 * de materia prima exige temporada de cosecha).</p>
 */
public enum TipoLote {
    MATERIA_PRIMA("Materia prima"),
    TRANSFORMADO("Producto transformado");

    private final String etiqueta;

    TipoLote(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public boolean exigeFichaTransformacion() {
        return this == TRANSFORMADO;
    }

    public boolean exigeTemporadaDeCosecha() {
        return this == MATERIA_PRIMA;
    }
}
