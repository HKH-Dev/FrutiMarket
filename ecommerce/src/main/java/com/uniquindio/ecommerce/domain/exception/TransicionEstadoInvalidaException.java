package com.uniquindio.ecommerce.domain.exception;

/** Se intento mover un lote a un estado al que no puede llegar desde su estado actual. */
public class TransicionEstadoInvalidaException extends ReglaDeNegocioVioladaException {

    private static final long serialVersionUID = 1L;

    public TransicionEstadoInvalidaException(String estadoActual, String estadoDestino, String operacion) {
        super("INV-ESTADO",
                "No se puede ejecutar '" + operacion + "': un lote en estado "
                        + estadoActual + " no puede pasar a " + estadoDestino + ".");
    }
}
