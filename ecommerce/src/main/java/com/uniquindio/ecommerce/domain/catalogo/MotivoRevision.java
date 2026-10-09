package com.uniquindio.ecommerce.domain.catalogo;

/**
 * Razon por la que un lote queda detenido en {@link EstadoLote#EN_REVISION}, y si basta
 * una validacion manual para liberarlo o hay que corregir un dato antes.
 */
public enum MotivoRevision {

    RUPTURA_CADENA_FRIO("R11", "Ruptura de la cadena de frio", true),
    INCIDENCIA("R11", "Incidencia reportada en la custodia", true),
    CALIDAD_NO_COINCIDE("R18", "Calidad real inferior a la declarada", false);

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

    public boolean esResolubleConValidacion() {
        return resolubleConValidacion;
    }
}
