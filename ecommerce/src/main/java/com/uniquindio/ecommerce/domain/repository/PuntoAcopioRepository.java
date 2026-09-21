package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.PuntoAcopio;
import java.util.Optional;

public interface PuntoAcopioRepository {
    Optional<PuntoAcopio> buscarPorId(String id);
}