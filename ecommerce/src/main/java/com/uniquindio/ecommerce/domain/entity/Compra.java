package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Agregado raiz: compra de un comprador. {@link DetalleCompra} es entidad interna y
 * referencia al lote solo por su {@code LoteId}. El id de la compra es tambien el
 * {@link PedidoId} con el que se reserva en cada lote.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li><b>INV-COMPRA-VACIA:</b> nunca puede confirmarse una compra sin detalles.</li>
 *   <li><b>INV-COMPRA-ESTADO:</b> nunca puede agregarse detalles, confirmarse ni cancelarse si no esta PENDIENTE.</li>
 *   <li><b>INV-COMPRA-REEMBOLSO:</b> nunca puede reembolsarse una compra que no este COMPLETADA.</li>
 *   <li><b>INV-COMPRA-TOTAL:</b> el total siempre es la suma de los subtotales de sus detalles.</li>
 * </ul>
 */
public class Compra {

    private final PedidoId id;
    private final CompradorId compradorId;
    private final Instant fecha;
    private final List<DetalleCompra> detalles;
    private EstadoCompra estado;

    private Compra(Builder builder) {
        this.id = builder.id;
        this.compradorId = builder.compradorId;
        this.fecha = builder.fecha;
        this.detalles = new ArrayList<>();
        this.estado = EstadoCompra.PENDIENTE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void agregarDetalle(DetalleCompra detalle) {
        ReglaDeNegocioVioladaException.validar(detalle != null, "INV-COMPRA-DETALLE", "El detalle es obligatorio.");
        exigirPendiente("agregar detalles a");
        detalles.add(detalle);
    }

    public void confirmar() {
        exigirPendiente("confirmar");
        ReglaDeNegocioVioladaException.validar(!detalles.isEmpty(), "INV-COMPRA-VACIA",
                "No se puede confirmar una compra sin detalles.");
        this.estado = EstadoCompra.COMPLETADA;
    }

    public void cancelar() {
        exigirPendiente("cancelar");
        this.estado = EstadoCompra.CANCELADA;
    }

    public void reembolsar() {
        ReglaDeNegocioVioladaException.validar(estado == EstadoCompra.COMPLETADA, "INV-COMPRA-REEMBOLSO",
                "Solo se puede reembolsar una compra COMPLETADA. Estado actual: " + estado);
        this.estado = EstadoCompra.REEMBOLSADA;
    }

    public BigDecimal total() {
        return detalles.stream()
                .map(DetalleCompra::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void exigirPendiente(String operacion) {
        ReglaDeNegocioVioladaException.validar(estado == EstadoCompra.PENDIENTE, "INV-COMPRA-ESTADO",
                "No se puede " + operacion + " una compra en estado " + estado + ".");
    }

    public PedidoId getId() { return id; }
    public CompradorId getCompradorId() { return compradorId; }
    public Instant getFecha() { return fecha; }
    public EstadoCompra getEstado() { return estado; }
    public List<DetalleCompra> getDetalles() { return List.copyOf(detalles); }

    @Override
    public boolean equals(Object o) {
        return o instanceof Compra otra && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final class Builder {
        private PedidoId id = PedidoId.nuevo();
        private CompradorId compradorId;
        private Instant fecha;

        private Builder() {
        }

        public Builder id(PedidoId id) { this.id = id; return this; }
        public Builder compradorId(CompradorId compradorId) { this.compradorId = compradorId; return this; }
        public Builder fecha(Instant fecha) { this.fecha = fecha; return this; }

        public Compra build() {
            ReglaDeNegocioVioladaException.validar(id != null, "INV-COMPRA", "La compra requiere id.");
            ReglaDeNegocioVioladaException.validar(compradorId != null, "INV-COMPRA", "La compra requiere un comprador.");
            ReglaDeNegocioVioladaException.validar(fecha != null, "INV-COMPRA", "La compra requiere fecha.");
            return new Compra(this);
        }
    }
}
