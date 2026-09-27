package com.uniquindio.ecommerce.domain.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class DetalleCompra {

    private UUID detalleId;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleCompra(int cantidad, BigDecimal precioUnitario) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El precio unitario no puede ser negativo."
            );
        }

        this.detalleId = UUID.randomUUID();
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() {
        subtotal = precioUnitario.multiply(
                BigDecimal.valueOf(cantidad)
        );

        return subtotal;
    }

    public UUID getDetalleId() {
        return detalleId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}