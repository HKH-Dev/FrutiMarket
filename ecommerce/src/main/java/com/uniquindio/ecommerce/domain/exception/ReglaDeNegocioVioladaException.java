package com.uniquindio.ecommerce.domain.exception;

/** Se viola una regla del negocio. {@code codigoRegla} indica cual, por ejemplo "R3" o "INV-CANTIDAD". */
public class ReglaDeNegocioVioladaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigoRegla;

    public ReglaDeNegocioVioladaException(String codigoRegla, String mensaje) {
        super(mensaje);
        this.codigoRegla = codigoRegla;
    }

    public static void validar(boolean condicion, String codigoRegla, String mensaje) {
        if (!condicion) {
            throw new ReglaDeNegocioVioladaException(codigoRegla, mensaje);
        }
    }

    public String codigoRegla() {
        return codigoRegla;
    }

    @Override
    public String toString() {
        return "[" + codigoRegla + "] " + getMessage();
    }
}
