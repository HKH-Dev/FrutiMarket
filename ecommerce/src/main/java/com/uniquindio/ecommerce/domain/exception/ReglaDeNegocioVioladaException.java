package com.uniquindio.ecommerce.domain.exception;


public class ReglaDeNegocioVioladaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigoRegla;

    public ReglaDeNegocioVioladaException(String codigoRegla, String mensaje) {
        super(mensaje);
        this.codigoRegla = codigoRegla;
    }

    public ReglaDeNegocioVioladaException(String mensaje) {
        this("INV", mensaje);
    }

    /** Codigo de la regla violada, por ejemplo {@code "R10"} o {@code "INV-CANTIDAD"}. */
    public String codigoRegla() {
        return codigoRegla;
    }

    @Override
    public String toString() {
        return "[" + codigoRegla + "] " + getMessage();
    }
}
