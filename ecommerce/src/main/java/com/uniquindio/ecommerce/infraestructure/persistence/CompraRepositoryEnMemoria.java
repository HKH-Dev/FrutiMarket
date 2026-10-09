package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CompraRepositoryEnMemoria implements CompraRepository {

    private final Map<PedidoId, Compra> compras = new HashMap<>();

    @Override
    public void registrar(Compra compra) {
        compras.put(compra.getId(), compra);
    }

    @Override
    public Optional<Compra> obtenerCompra(PedidoId id) {
        return Optional.ofNullable(compras.get(id));
    }

    @Override
    public List<Compra> comprasDelComprador(CompradorId compradorId) {
        return compras.values().stream()
                .filter(compra -> compra.getCompradorId().equals(compradorId))
                .toList();
    }
}
