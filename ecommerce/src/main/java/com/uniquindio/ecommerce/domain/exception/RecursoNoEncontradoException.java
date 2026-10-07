package com.uniquindio.ecommerce.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String tipo, Object id) {
        super("No existe " + tipo + " con id: " + id);
    }
}