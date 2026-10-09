package com.uniquindio.ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.time.Instant;
import java.util.Objects;

/**
 * Value Object interno del agregado {@code Lote}: cantidad comprometida con un pedido.
 * Solo el lote la crea; por eso la factoria es de visibilidad de paquete.
 */
public final class ReservaLote {

    private final PedidoId pedido;
    private final Cantidad cantidad;
    private final Instant momento;

    private ReservaLote(PedidoId pedido, Cantidad cantidad, Instant momento) {
        this.pedido = pedido;
        this.cantidad = cantidad;
        this.momento = momento;
    }

    static ReservaLote crear(PedidoId pedido, Cantidad cantidad, Instant momento) {
        ReglaDeNegocioVioladaException.validar(pedido != null, "INV-RESERVA", "Una reserva debe referirse a un pedido.");
        ReglaDeNegocioVioladaException.validar(cantidad != null, "INV-RESERVA", "Una reserva debe tener cantidad.");
        ReglaDeNegocioVioladaException.validar(momento != null, "INV-RESERVA", "Una reserva debe tener momento.");
        return new ReservaLote(pedido, cantidad, momento);
    }

    public PedidoId pedido() { return pedido; }
    public Cantidad cantidad() { return cantidad; }
    public Instant momento() { return momento; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ReservaLote otra && pedido.equals(otra.pedido)
                && cantidad.equals(otra.cantidad) && momento.equals(otra.momento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pedido, cantidad, momento);
    }
}
