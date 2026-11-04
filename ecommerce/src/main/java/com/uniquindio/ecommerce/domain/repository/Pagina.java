package com.uniquindio.ecommerce.domain.repository;

import java.util.List;
import java.util.function.Function;

/**
 * Una pagina de resultados. Es del dominio a proposito: asi el dominio y los casos de uso
 * no dependen de Page de Spring; la conversion se hace en el adaptador JPA.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public <R> Pagina<R> mapear(Function<T, R> conversion) {
        return new Pagina<>(contenido.stream().map(conversion).toList(), pagina, tamano, totalElementos, totalPaginas);
    }
}