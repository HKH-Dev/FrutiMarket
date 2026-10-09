package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    void registrar(Producto producto);

    Optional<Producto> obtenerProducto(ProductoId id);

    List<Producto> productosActivos();
}
