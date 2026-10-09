package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProductoRepositoryEnMemoria implements ProductoRepository {

    private final Map<ProductoId, Producto> productos = new HashMap<>();

    @Override
    public void registrar(Producto producto) {
        productos.put(producto.id(), producto);
    }

    @Override
    public Optional<Producto> obtenerProducto(ProductoId id) {
        return Optional.ofNullable(productos.get(id));
    }

    @Override
    public List<Producto> productosActivos() {
        return productos.values().stream().filter(Producto::estaActivo).toList();
    }
}
