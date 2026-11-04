package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.domain.consulta.Pagina;

import java.util.List;
import java.util.function.Function;

/** Respuesta paginada: el contenido de la pagina, el total de elementos y el total de paginas. */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <D, T> PaginaResponse<T> desde(Pagina<D> pagina, Function<D, T> conversion) {
        return new PaginaResponse<>(pagina.contenido().stream().map(conversion).toList(), pagina.numero(),
                pagina.tamano(), pagina.totalElementos(), pagina.totalPaginas());
    }
}
