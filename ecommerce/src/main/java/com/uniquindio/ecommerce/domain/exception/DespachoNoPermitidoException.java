package com.uniquindio.ecommerce.domain.exception;

/** Reglas 9, 10, 11, 12 y 17: condiciones que bloquean el despacho de un lote. */
public class DespachoNoPermitidoException extends ReglaDeNegocioVioladaException {

    private static final long serialVersionUID = 1L;

    public DespachoNoPermitidoException(String codigoRegla, String motivo) {
        super(codigoRegla, "El lote no puede despacharse: " + motivo);
    }
}
