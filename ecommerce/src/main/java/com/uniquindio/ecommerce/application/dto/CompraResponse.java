package com.uniquindio.ecommerce.application.dto;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response: resultado de {@code RealizarCompraUseCase}, el comprobante que ve el comprador.
 *
 * @param compraId UUID de la compra; necesario para consultarla o pedir reembolso despues.
 * @param estado   nombre de {@code EstadoCompra}; confirma si la compra quedo COMPLETADA.
 * @param fecha    momento de la compra, para el historial del comprador.
 * @param total    suma de subtotales (INV-COMPRA-TOTAL); lo calcula el dominio, nunca el cliente.
 * @param detalles lineas compradas, para que el comprador verifique que pago.
 */
public record CompraResponse(
        String compraId,
        String estado,
        LocalDateTime fecha,
        BigDecimal total,
        List<DetalleResponse> detalles) {

    /**
     * @param loteId         lote del que salio el producto (trazabilidad hacia el campesino).
     * @param cantidad       unidades compradas.
     * @param precioUnitario precio aplicado, tomado del lote al momento de la compra.
     * @param subtotal       cantidad x precio, para que el total sea verificable.
     */
    public record DetalleResponse(String loteId, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {

        static DetalleResponse desde(DetalleCompra detalle) {
            return new DetalleResponse(detalle.getLote().toString(), detalle.getCantidad(),
                    detalle.getPrecioUnitario(), detalle.getSubtotal());
        }
    }

    public static CompraResponse desde(Compra compra) {
        return new CompraResponse(
                compra.getCompraId().toString(),
                compra.getEstado().name(),
                compra.getFecha(),
                compra.getTotal(),
                compra.getDetalles().stream().map(DetalleResponse::desde).toList());
    }
}
