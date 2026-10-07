package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.eccommerce.dominio.valueobject.catalogo.Cantidad;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.PedidoId;

import java.time.Instant;
import java.util.Objects;

/**
 * Cantidad de un lote comprometida con un pedido concreto.
 *
 * <p><b>Value Object (record) dentro del agregado.</b> No se modifica: una reserva
 * se crea, y luego se confirma o se libera quitandola de la lista del lote. Esa
 * lista es lo que permite cumplir la regla 5, que prohibe retirar un lote con
 * pedidos activos.</p>
 *
 * <p>{@code pedido} es un identificador, no una referencia al agregado
 * {@code Pedido}: el lote nunca navega hacia el pedido.</p>
 */
public record ReservaLote(PedidoId pedido, Cantidad cantidad, Instant momento) {

    public ReservaLote {
        Objects.requireNonNull(pedido, "Una reserva debe referirse a un pedido.");
        Objects.requireNonNull(cantidad, "Una reserva debe tener cantidad.");
        Objects.requireNonNull(momento, "Una reserva debe tener momento de creacion.");
    }
}
