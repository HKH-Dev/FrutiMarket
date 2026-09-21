package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import java.util.Optional;

public interface CampesinoRepository {
    Optional<Campesino> buscarPorId(String id);
}