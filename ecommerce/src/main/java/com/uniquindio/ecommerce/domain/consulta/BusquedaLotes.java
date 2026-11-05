package com.uniquindio.ecommerce.domain.consulta;

import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.math.BigDecimal;

/**
 * Criterios para buscar lotes en el catalogo. Solo se buscan lotes que se pueden comprar:
 * publicados y con cantidad disponible (los retirados, que es el borrado logico, nunca salen).
 *
 * @param texto        busca en el nombre del producto y en el codigo del lote; vacio = todos.
 * @param tipoCultivo  filtro opcional (null = cualquiera).
 * @param precioMaximo filtro opcional por precio de finca (null = sin limite).
 * @param pagina       numero de pagina, desde 0.
 * @param tamano       elementos por pagina.
 */
public record BusquedaLotes(String texto, TipoCultivo tipoCultivo, BigDecimal precioMaximo, int pagina, int tamano) {

    public BusquedaLotes {
        texto = texto == null ? "" : texto.trim();
    }
}
