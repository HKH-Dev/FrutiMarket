package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.Optional;

public interface CompradorRepository {

    void registrar(Comprador comprador);

    Optional<Comprador> obtenerComprador(CompradorId id);
}
