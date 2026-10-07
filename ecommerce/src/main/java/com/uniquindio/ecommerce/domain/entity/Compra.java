package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz de la compra. {@link DetalleCompra} es entidad interna: solo se
 * crea y se modifica a traves de la compra. Los lotes se referencian por
 * {@code LoteId}, nunca por objeto.
 *
 * <p>Invariantes del agregado:</p>
 * <ul>
 *   <li><b>INV-COMPRA-VACIA:</b> nunca puede confirmarse una compra sin al menos un detalle.</li>
 *   <li><b>INV-COMPRA-ESTADO:</b> nunca puede agregarse productos, confirmarse ni cancelarse
 *       una compra que ya no esta PENDIENTE.</li>
 *   <li><b>INV-COMPRA-TOTAL:</b> el total siempre debe ser la suma de los subtotales de sus detalles.</li>
 *   <li><b>INV-COMPRA-REEMBOLSO:</b> nunca puede solicitarse reembolso de una compra que no este COMPLETADA.</li>
 * </ul>
 */
public class Compra {

    private final UUID compraId;
    private final LocalDateTime fecha;
    private EstadoCompra estado;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String numeroTransaccion;

    private final List<DetalleCompra> detalles;

    public Compra() {
        this(UUID.randomUUID());
    }

    public Compra(UUID compraId) {
        this.compraId = Objects.requireNonNull(compraId, "La compra requiere un identificador.");
        this.fecha = LocalDateTime.now();
        this.estado = EstadoCompra.PENDIENTE;
        this.subtotal = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }

    public void agregarProducto(DetalleCompra detalle) {
        if (detalle == null) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-DETALLE",
                    "El detalle de compra no puede ser nulo.");
        }
        exigirPendiente("agregar productos a");
        detalles.add(detalle);
        calcularTotal();
    }

    public BigDecimal calcularTotal() {
        subtotal = detalles.stream()
                .map(DetalleCompra::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal;
        return total;
    }

    public void confirmar() {
        exigirPendiente("confirmar");
        if (detalles.isEmpty()) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-VACIA",
                    "No se puede confirmar una compra sin productos.");
        }
        calcularTotal();
        estado = EstadoCompra.COMPLETADA;
    }

    public void cancelar() {
        exigirPendiente("cancelar");
        estado = EstadoCompra.CANCELADA;
    }

    public Reembolso solicitarReembolso() {
        if (!estaCompletada()) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-REEMBOLSO",
                    "Solo se puede solicitar un reembolso de una compra completada.");
        }
        return new Reembolso("Reembolso de la compra", total);
    }

    public boolean estaCompletada() {
        return estado == EstadoCompra.COMPLETADA;
    }

    private void exigirPendiente(String operacion) {
        if (estado != EstadoCompra.PENDIENTE) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-ESTADO",
                    "No se puede " + operacion + " una compra en estado " + estado
                            + "; solo se permite si esta PENDIENTE.");
        }
    }

    public UUID getCompraId() {
        return compraId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getNumeroTransaccion() {
        return numeroTransaccion;
    }

    public List<DetalleCompra> getDetalles() {
        return List.copyOf(detalles);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Compra otra && compraId.equals(otra.compraId);
    }

    @Override
    public int hashCode() {
        return compraId.hashCode();
    }
}