package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

import java.util.Optional;

public interface CampesinoRepository {

    void registrar(Campesino campesino);

    Optional<Campesino> obtenerCampesino(CampesinoId id);
}
