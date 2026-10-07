package com.uniquindio.ecommerce.domain.exception;

import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;

/** No existe un lote con el identificador solicitado. */
public class LoteNoEncontradoException extends ReglaDeNegocioVioladaException {

    private static final long serialVersionUID = 1L;

    public LoteNoEncontradoException(LoteId id) {
        super("NOT-FOUND", "No existe un lote con identificador " + id + ".");
    }
}
