package com.uniquindio.ecommerce.domain.exception;

/** Reglas 1, 2 y 8: faltan datos obligatorios para publicar el lote. */
public class LoteNoPublicableException extends ReglaDeNegocioVioladaException {

    private static final long serialVersionUID = 1L;

    public LoteNoPublicableException(String codigoRegla, String motivo) {
        super(codigoRegla, "El lote no puede publicarse: " + motivo);
    }
}
