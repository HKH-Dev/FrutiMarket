package com.uniquindio.ecommerce.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String tipo, Object id) {
        super("No existe " + tipo + " con id: " + id);
    }
}
