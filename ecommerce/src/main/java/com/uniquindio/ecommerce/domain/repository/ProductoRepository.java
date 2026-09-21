package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Producto;

import java.util.Optional;

public interface ProductoRepository {
    Optional<Producto> buscarPorId(String id);
}
