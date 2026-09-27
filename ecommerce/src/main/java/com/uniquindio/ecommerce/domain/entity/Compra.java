package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Compra {

    private UUID compraId;
    private LocalDateTime fecha;
    private EstadoCompra estado;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String numeroTransaccion;

    private List<DetalleCompra> detalles;

    public Compra() {
        this.compraId = UUID.randomUUID();
        this.fecha = LocalDateTime.now();
        this.estado = EstadoCompra.PENDIENTE;
        this.subtotal = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }

    public void agregarProducto(DetalleCompra detalle) {

        if (detalle == null) {
            throw new IllegalArgumentException(
                    "El detalle de compra no puede ser nulo."
            );
        }

        if (estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                    "No se pueden agregar productos a una compra que no está pendiente."
            );
        }

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

        if (estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede confirmar una compra pendiente."
            );
        }

        if (detalles.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede confirmar una compra sin productos."
            );
        }

        calcularTotal();

        estado = EstadoCompra.COMPLETADA;
    }

    public void cancelar() {

        if (estado != EstadoCompra.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede cancelar una compra pendiente."
            );
        }

        estado = EstadoCompra.CANCELADA;
    }

    public Reembolso solicitarReembolso() {

        if (!estaCompletada()) {
            throw new IllegalStateException(
                    "Solo se puede solicitar un reembolso de una compra completada."
            );
        }

        return new Reembolso(
                "Reembolso de la compra",
                total
        );
    }

    public boolean estaCompletada() {
        return estado == EstadoCompra.COMPLETADA;
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
        return new ArrayList<>(detalles);
    }
}


