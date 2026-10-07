package com.uniquindio.ecommerce.domain.catalogo;

/**
 * Razon por la que un lote queda detenido en {@link EstadoLote#EN_REVISION}.
 *
 * <p>Cada constante apunta a la regla del documento de Entrega 1 que la origina,
 * y declara si la revision puede resolverse validando el lote o si obliga a
 * corregir un dato antes.</p>
 */
public enum MotivoRevision {

    /** Regla 11: el lote perdio las condiciones requeridas de conservacion. */
    RUPTURA_CADENA_FRIO("R11", "Ruptura de la cadena de frio", true),

    /** Regla 11: humedad o temperatura fuera de la condicion declarada. */
    CONDICION_CONSERVACION_PERDIDA("R11", "Condicion de conservacion perdida", true),

    /** Regla 18: las caracteristicas reales no coinciden con el calibre declarado. */
    CALIBRE_NO_COINCIDE("R18", "Calibre real distinto del declarado", false),

    /** Regla 17: certificacion de origen vencida. */
    CERTIFICACION_VENCIDA("R17", "Certificacion de origen vencida", false),

    /** Merma real muy por encima de la declarada. */
    MERMA_EXCESIVA("INV-MERMA", "Merma real superior a la declarada", true);

    private final String codigoRegla;
    private final String descripcion;
    private final boolean resolubleConValidacion;

    MotivoRevision(String codigoRegla, String descripcion, boolean resolubleConValidacion) {
        this.codigoRegla = codigoRegla;
        this.descripcion = descripcion;
        this.resolubleConValidacion = resolubleConValidacion;
    }

    public String codigoRegla() {
        return codigoRegla;
    }

    public String descripcion() {
        return descripcion;
    }

    /**
     * {@code true} si basta una validacion manual para liberar el lote;
     * {@code false} si primero hay que corregir el dato de origen (calibre, sello).
     */
    public boolean esResolubleConValidacion() {
        return resolubleConValidacion;
    }
}
