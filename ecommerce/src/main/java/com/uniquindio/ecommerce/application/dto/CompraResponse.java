package com.uniquindio.ecommerce.application.dto;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response: comprobante de {@code RealizarCompraUseCase}.
 *
 * @param compraId    id de la compra, para consultarla o reembolsarla.
 * @param compradorId quien compro.
 * @param estado      confirma si quedo COMPLETADA.
 * @param fecha       momento de la compra.
 * @param total       suma de subtotales (INV-COMPRA-TOTAL), calculada por el dominio.
 * @param detalles    lineas compradas.
 */
public record CompraResponse(
        UUID compraId,
        UUID compradorId,
        String estado,
        Instant fecha,
        BigDecimal total,
        List<Detalle> detalles) {

    /**
     * @param loteId         lote del que salio el producto (trazabilidad hacia el campesino).
     * @param cantidad       cantidad comprada.
     * @param unidadMedida   unidad de la cantidad.
     * @param precioUnitario precio de finca aplicado.
     * @param subtotal       cantidad x precio, para que el total sea verificable.
     */
    public record Detalle(UUID loteId, BigDecimal cantidad, String unidadMedida,
                          BigDecimal precioUnitario, BigDecimal subtotal) {

        static Detalle desde(DetalleCompra detalle) {
            return new Detalle(detalle.getLote().valor(), detalle.getCantidad().valor(),
                    detalle.getCantidad().unidad().simbolo(),
                    detalle.getPrecioUnitario().valorPorUnidad(), detalle.subtotal());
        }
    }

    public static CompraResponse desde(Compra compra) {
        return new CompraResponse(
                compra.getId().valor(),
                compra.getCompradorId().valor(),
                compra.getEstado().name(),
                compra.getFecha(),
                compra.total(),
                compra.getDetalles().stream().map(Detalle::desde).toList());
    }
}
