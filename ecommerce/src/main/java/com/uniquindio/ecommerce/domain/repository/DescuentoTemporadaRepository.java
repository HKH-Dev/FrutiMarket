package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DescuentoTemporadaRepository {

    void guardar(DescuentoTemporada descuento);

    Optional<DescuentoTemporada> obtener(UUID id);

    List<DescuentoTemporada> todos();
}
