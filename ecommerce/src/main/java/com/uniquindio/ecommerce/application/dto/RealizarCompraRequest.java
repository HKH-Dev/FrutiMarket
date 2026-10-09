package com.uniquindio.ecommerce.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request: un comprador compra de uno o varios lotes. Mapea a {@code Lote.reservar(...)}
 * por item, {@code Compra.agregarDetalle(...)} y {@code Compra.confirmar()}.
 * No trae precio: se toma del lote, para que el cliente no pueda fijarlo.
 *
 * @param compradorId quien compra; la compra no existe sin comprador.
 * @param items       lineas de la compra; vacia no se puede confirmar (INV-COMPRA-VACIA).
 */
public record RealizarCompraRequest(UUID compradorId, List<Item> items) {

    /**
     * @param loteId   lote del que se compra.
     * @param cantidad cuanto se compra, en la unidad del lote; no puede superar su disponible (regla 3).
     */
    public record Item(UUID loteId, BigDecimal cantidad) {
    }
}
