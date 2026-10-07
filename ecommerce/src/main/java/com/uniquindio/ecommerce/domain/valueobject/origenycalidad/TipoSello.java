package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

/**
 * Cualidad verificable que certifica un sello de origen.
 *
 * <p><b>Enum y no texto libre</b>: el glosario es explicito en que un sello no es
 * una etiqueta de calidad cualquiera, sino un valor cerrado que el sistema puede
 * validar y por el que el comprador puede filtrar.</p>
 */
public enum TipoSello {
    ORGANICO("Produccion organica", 730),
    SEMILLA_NATIVA("Variedad de semilla nativa", 1095),
    ESCALA_ARTESANAL("Escala artesanal", 365),
    COMERCIO_JUSTO("Comercio justo", 730),
    BUENAS_PRACTICAS_AGRICOLAS("Buenas practicas agricolas", 1095);

    private final String etiqueta;
    private final int vigenciaDias;

    TipoSello(String etiqueta, int vigenciaDias) {
        this.etiqueta = etiqueta;
        this.vigenciaDias = vigenciaDias;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Vigencia estandar del sello, contada desde su fecha de emision. */
    public int vigenciaDias() {
        return vigenciaDias;
    }
}
