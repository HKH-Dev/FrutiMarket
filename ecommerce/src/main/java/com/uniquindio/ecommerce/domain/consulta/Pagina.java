package com.uniquindio.ecommerce.domain.consulta;

import java.util.List;
import java.util.function.Function;

/**
 * Una pagina de resultados. Es propia del dominio a proposito: el dominio no conoce el
 * {@code Page} de Spring Data; el adaptador JPA hace la conversion.
 *
 * @param contenido       elementos de esta pagina.
 * @param numero          numero de pagina, empezando en 0.
 * @param tamano          cuantos elementos se pidieron por pagina.
 * @param totalElementos  cuantos elementos cumplen la busqueda en total.
 * @param totalPaginas    cuantas paginas hay en total.
 */
public record Pagina<T>(List<T> contenido, int numero, int tamano, long totalElementos, int totalPaginas) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public <R> Pagina<R> map(Function<? super T, ? extends R> conversion) {
        return new Pagina<>(contenido.stream().<R>map(conversion).toList(), numero, tamano, totalElementos, totalPaginas);
    }
}
