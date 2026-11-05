package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.CampesinoMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CampesinoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class CampesinoRepositoryJpa implements CampesinoRepository {

    private final CampesinoJpaRepository jpa;

    public CampesinoRepositoryJpa(CampesinoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(Campesino campesino) {
        jpa.save(CampesinoMapper.aEntidad(campesino));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Campesino> obtenerCampesino(CampesinoId id) {
        return jpa.findById(id.valor()).map(CampesinoMapper::aDominio);
    }
}
