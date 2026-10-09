package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.util.List;
import java.util.Optional;

public interface CompraRepository {

    void registrar(Compra compra);

    Optional<Compra> obtenerCompra(PedidoId id);

    List<Compra> comprasDelComprador(CompradorId comprador);
}
