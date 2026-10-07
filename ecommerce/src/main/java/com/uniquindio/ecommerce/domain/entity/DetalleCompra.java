package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/** Entidad interna del agregado {@link Compra}: una linea de compra sobre un lote. */
public class DetalleCompra {

    private final UUID detalleId;
    private final LoteId lote;
    private final int cantidad;
    private final BigDecimal precioUnitario;

    public DetalleCompra(LoteId lote, int cantidad, BigDecimal precioUnitario) {
        Objects.requireNonNull(lote, "El detalle debe referirse a un lote.");
        if (cantidad <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-DETALLE",
                    "La cantidad debe ser mayor que cero.");
        }
        if (precioUnitario == null || precioUnitario.signum() <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-COMPRA-DETALLE",
                    "El precio unitario debe ser mayor que cero.");
        }
        this.detalleId = UUID.randomUUID();
        this.lote = lote;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public UUID getDetalleId() {
        return detalleId;
    }

    public LoteId getLote() {
        return lote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return calcularSubtotal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof DetalleCompra otro && detalleId.equals(otro.detalleId);
    }

    @Override
    public int hashCode() {
        return detalleId.hashCode();
    }
}
