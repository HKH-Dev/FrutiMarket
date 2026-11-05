package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompraEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.DetalleCompraEmbeddable;

import java.util.ArrayList;
import java.util.List;

/** Convierte entre el agregado {@link Compra} (con sus detalles) y {@link CompraEntity}. */
public final class CompraMapper {

    private CompraMapper() {
    }

    public static CompraEntity aEntidad(Compra compra) {
        CompraEntity e = new CompraEntity();
        e.setId(compra.getId().valor());
        e.setCompradorId(compra.getCompradorId().valor());
        e.setFecha(compra.getFecha());
        e.setEstado(compra.getEstado());
        e.setDetalles(new ArrayList<>(compra.getDetalles().stream().map(d -> new DetalleCompraEmbeddable(
                d.getId(), d.getLote().valor(), d.getCantidad().valor(), d.getCantidad().unidad(),
                d.getPrecioUnitario().valorPorUnidad())).toList()));
        return e;
    }

    public static Compra aDominio(CompraEntity e) {
        List<DetalleCompra> detalles = e.getDetalles().stream().map(d -> DetalleCompra.reconstituir(
                d.getDetalleId(), LoteId.de(d.getLoteId()), Cantidad.de(d.getCantidad(), d.getUnidad()),
                PrecioFinca.de(d.getPrecioUnitario(), d.getUnidad()))).toList();
        return Compra.reconstituir(PedidoId.de(e.getId()), CompradorId.de(e.getCompradorId()), e.getFecha(),
                e.getEstado(), detalles);
    }
}
