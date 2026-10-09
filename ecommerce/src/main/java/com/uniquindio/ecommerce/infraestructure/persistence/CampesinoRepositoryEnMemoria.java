package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CampesinoRepositoryEnMemoria implements CampesinoRepository {

    private final Map<CampesinoId, Campesino> campesinos = new HashMap<>();

    @Override
    public void registrar(Campesino campesino) {
        campesinos.put(campesino.getId(), campesino);
    }

    @Override
    public Optional<Campesino> obtenerCampesino(CampesinoId id) {
        return Optional.ofNullable(campesinos.get(id));
    }
}
