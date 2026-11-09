package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.time.Instant;
import java.util.Set;

public interface FavoritoRepository {

    /** No falla si el producto ya era favorito. */
    void agregar(CompradorId comprador, ProductoId producto, Instant momento);

    void quitar(CompradorId comprador, ProductoId producto);

    Set<ProductoId> delComprador(CompradorId comprador);
}
