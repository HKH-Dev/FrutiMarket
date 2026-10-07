package com.uniquindio.ecommerce.application.dto;

import com.uniquindio.ecommerce.domain.catalogo.Lote;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response: datos de un lote para mostrarlo en el catalogo o al campesino.
 * Sale de {@code ConsultarLoteUseCase} / {@code LoteRepository.buscarPorId(...)}.
 *
 * <p>Se devuelve este record y no el {@code Lote}: si el controlador recibiera el
 * agregado podria llamar {@code despachar()} o {@code retirar()} por accidente, y el
 * JSON expondria reservas y eventos internos.</p>
 *
 * @param id                 UUID del lote; el cliente lo necesita para reservar o consultar trazabilidad.
 * @param codigo             codigo legible con el que el campesino identifica el lote.
 * @param productoId         UUID del producto, para enlazar con la ficha del catalogo.
 * @param estado             etiqueta del {@code EstadoLote}; le dice al comprador si puede pedir.
 * @param cantidadDisponible cantidad que aun se puede reservar (ya descontadas merma y reservas).
 * @param unidadMedida       simbolo de la unidad, sin el cual la cantidad no se puede interpretar.
 * @param precioPorUnidad    precio de finca: el valor que recibe el productor, propuesta de valor del negocio.
 * @param moneda             moneda del precio.
 * @param fechaCosecha       frescura del producto, dato que el comprador usa para decidir.
 * @param fechaLimiteConsumo hasta cuando se puede consumir; null si el lote no es perecedero.
 */
public record LoteResponse(
        String id,
        String codigo,
        String productoId,
        String estado,
        BigDecimal cantidadDisponible,
        String unidadMedida,
        BigDecimal precioPorUnidad,
        String moneda,
        LocalDate fechaCosecha,
        LocalDate fechaLimiteConsumo) {

    public static LoteResponse desde(Lote lote) {
        return new LoteResponse(
                lote.id().toString(),
                lote.codigo(),
                lote.producto().toString(),
                lote.estado().etiqueta(),
                lote.cantidadDisponible().valor(),
                lote.cantidadDisponible().unidad().simbolo(),
                lote.precioFinca() == null ? null : lote.precioFinca().valorPorUnidad(),
                lote.precioFinca() == null ? null : lote.precioFinca().moneda().name(),
                lote.fechaCosecha(),
                lote.fechaLimiteConsumo().orElse(null));
    }
}
