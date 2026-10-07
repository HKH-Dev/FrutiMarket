package com.uniquindio.ecommerce.application.dto;

import java.util.List;

/**
 * Request: un comprador compra producto de uno o varios lotes.
 * Mapea a {@code Lote.reservar(...)} por cada item, {@code Compra.agregarProducto(...)}
 * y finalmente {@code Compra.confirmar()}.
 *
 * <p>No trae precio: el precio unitario se toma del {@code PrecioFinca} del lote en el
 * servidor. Si viniera del cliente, cualquiera podria comprar al precio que quisiera.</p>
 *
 * @param items lineas de la compra; no puede venir vacia porque una compra sin detalles
 *              no puede confirmarse (INV-COMPRA-VACIA).
 */
public record RealizarCompraRequest(List<ItemCompra> items) {

    /**
     * @param loteId   UUID del lote del que se compra; se usa para cargar el agregado {@code Lote}
     *                 y reservar (regla 3) y para el {@code LoteId} del {@code DetalleCompra}.
     * @param cantidad unidades a comprar; el lote la rechaza si supera su disponible (regla 3).
     */
    public record ItemCompra(String loteId, int cantidad) {
    }
}
