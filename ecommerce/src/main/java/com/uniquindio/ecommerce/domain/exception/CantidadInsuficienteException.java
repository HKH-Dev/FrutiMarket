package com.uniquindio.ecommerce.domain.exception;

/** Regla 3: un pedido no puede superar la cantidad disponible del lote. */
public class CantidadInsuficienteException extends ReglaDeNegocioVioladaException {

    private static final long serialVersionUID = 1L;

    public CantidadInsuficienteException(String solicitada, String disponible) {
        super("R3", "La cantidad solicitada (" + solicitada
                + ") supera la cantidad disponible del lote (" + disponible + ").");
    }
}
