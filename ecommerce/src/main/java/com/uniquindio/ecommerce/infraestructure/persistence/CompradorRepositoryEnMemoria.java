package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompradorRepositoryEnMemoria implements CompradorRepository {

    private final Map<CompradorId, Comprador> compradores = new HashMap<>();

    @Override
    public void registrar(Comprador comprador) {
        compradores.put(comprador.getId(), comprador);
    }

    @Override
    public Optional<Comprador> obtenerComprador(CompradorId id) {
        return Optional.ofNullable(compradores.get(id));
    }
}
