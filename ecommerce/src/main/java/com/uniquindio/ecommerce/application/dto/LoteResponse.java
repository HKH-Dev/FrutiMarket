package com.uniquindio.ecommerce.application.dto;

import com.uniquindio.ecommerce.domain.catalogo.Lote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response: lo que el comprador ve de un lote en el catalogo. Se devuelve esto y no el
 * {@code Lote}, para que la capa web no pueda invocar su comportamiento.
 *
 * @param id                 id del lote, para comprarlo o ver su trazabilidad.
 * @param codigo             codigo legible del lote.
 * @param productoId         producto del catalogo.
 * @param producto           nombre del producto.
 * @param campesinoId        quien lo vende.
 * @param estado             si se puede comprar.
 * @param cantidadDisponible lo que aun se puede comprar.
 * @param unidadMedida       unidad de cantidad y precio.
 * @param precioPorUnidad    precio de finca: lo que recibe el productor.
 * @param puntajeCalidad     0 a 100, para comparar con otros lotes del mismo producto.
 * @param categoriaCalidad   extra, primera, segunda o industrial.
 * @param fechaCosecha       frescura.
 * @param fechaLimiteConsumo hasta cuando se puede consumir; null si no es perecedero.
 */
public record LoteResponse(
        UUID id,
        String codigo,
        UUID productoId,
        String producto,
        UUID campesinoId,
        String estado,
        BigDecimal cantidadDisponible,
        String unidadMedida,
        BigDecimal precioPorUnidad,
        int puntajeCalidad,
        String categoriaCalidad,
        LocalDate fechaCosecha,
        LocalDate fechaLimiteConsumo) {

    public static LoteResponse desde(Lote lote) {
        return new LoteResponse(
                lote.id().valor(),
                lote.codigo(),
                lote.producto().valor(),
                lote.nombreProducto(),
                lote.campesinoResponsable().valor(),
                lote.estado().etiqueta(),
                lote.cantidadDisponible().valor(),
                lote.cantidadDisponible().unidad().simbolo(),
                lote.precioFinca() == null ? null : lote.precioFinca().valorPorUnidad(),
                lote.puntajeCalidad(),
                lote.calidadDeclarada().categoria().name(),
                lote.fechaCosecha(),
                lote.fechaLimiteConsumo().orElse(null));
    }
}
