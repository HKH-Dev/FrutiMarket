package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.domain.repository.Pagina;

import java.util.List;
import java.util.function.Function;

/** Respuesta paginada: el contenido de la pagina, el total de elementos y el total de paginas. */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <D, R> PaginaResponse<R> desde(Pagina<D> pagina, Function<D, R> conversion) {
        Pagina<R> convertida = pagina.mapear(conversion);
        return new PaginaResponse<>(convertida.contenido(), convertida.pagina(), convertida.tamano(),
                convertida.totalElementos(), convertida.totalPaginas());
    }
}