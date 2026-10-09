package com.uniquindio.ecommerce.domain.entity;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.math.BigDecimal;
import java.util.UUID;

/** Entidad interna del agregado {@link Compra}: una linea de compra sobre un lote. */
public class DetalleCompra {

    private final UUID id;
    private final LoteId lote;
    private final Cantidad cantidad;
    private final PrecioFinca precioUnitario;

    private DetalleCompra(UUID id, LoteId lote, Cantidad cantidad, PrecioFinca precioUnitario) {
        this.id = id;
        this.lote = lote;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public static DetalleCompra crear(LoteId lote, Cantidad cantidad, PrecioFinca precioUnitario) {
        ReglaDeNegocioVioladaException.validar(lote != null, "INV-COMPRA-DETALLE", "El detalle debe referirse a un lote.");
        ReglaDeNegocioVioladaException.validar(cantidad != null && !cantidad.esCero(), "INV-COMPRA-DETALLE",
                "La cantidad del detalle debe ser mayor que cero.");
        ReglaDeNegocioVioladaException.validar(precioUnitario != null, "INV-COMPRA-DETALLE",
                "El detalle requiere precio unitario.");
        ReglaDeNegocioVioladaException.validar(cantidad.unidad() == precioUnitario.unidadReferencia(), "INV-COMPRA-DETALLE",
                "La cantidad y el precio deben estar en la misma unidad.");
        return new DetalleCompra(UUID.randomUUID(), lote, cantidad, precioUnitario);
    }

    public BigDecimal subtotal() {
        return precioUnitario.totalPara(cantidad);
    }

    public UUID getId() { return id; }
    public LoteId getLote() { return lote; }
    public Cantidad getCantidad() { return cantidad; }
    public PrecioFinca getPrecioUnitario() { return precioUnitario; }

    @Override
    public boolean equals(Object o) {
        return o instanceof DetalleCompra otro && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
