package com.uniquindio.ecommerce.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String tipo, String id) {
        super("No existe " + tipo + " con id: " + id);
    }
}