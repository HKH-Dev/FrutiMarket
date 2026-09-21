package com.uniquindio.ecommerce.domain.exception;


public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) { super(mensaje); }

    public static void validar(boolean condicion, String mensaje) {
        if (!condicion) throw new ReglaNegocioException(mensaje);
    }
}

