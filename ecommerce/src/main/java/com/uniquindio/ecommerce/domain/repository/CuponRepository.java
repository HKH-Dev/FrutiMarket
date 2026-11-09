package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.List;
import java.util.Optional;

public interface CuponRepository {

    void guardar(Cupon cupon);

    Optional<Cupon> porCodigo(String codigo);

    List<Cupon> delComprador(CompradorId comprador);

    List<Cupon> todos();
}
