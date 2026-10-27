package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CampesinoEntity;
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
        CampesinoEntity e = new CampesinoEntity();
        e.setId(campesino.getId().valor());
        e.setNombre(campesino.getNombre());
        e.setNumeroIdentificacion(campesino.getNumeroIdentificacion());
        e.setEscalaProduccion(campesino.getEscalaProduccion());
        e.setAutorizadoParaPublicar(campesino.estaAutorizadoParaPublicar());
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Campesino> obtenerCampesino(CampesinoId id) {
        return jpa.findById(id.valor()).map(e -> {
            Campesino campesino = Campesino.builder()
                    .id(CampesinoId.de(e.getId()))
                    .nombre(e.getNombre())
                    .numeroIdentificacion(e.getNumeroIdentificacion())
                    .escalaProduccion(e.getEscalaProduccion())
                    .build();
            if (e.isAutorizadoParaPublicar()) {
                campesino.autorizarParaPublicar();
            }
            return campesino;
        });
    }
}
