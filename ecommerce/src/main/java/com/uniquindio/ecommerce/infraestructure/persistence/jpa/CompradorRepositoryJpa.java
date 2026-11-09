package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.CompradorMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CompradorJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class CompradorRepositoryJpa implements CompradorRepository {

    private final CompradorJpaRepository jpa;

    public CompradorRepositoryJpa(CompradorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(Comprador comprador) {
        jpa.save(CompradorMapper.aEntidad(comprador));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprador> obtenerComprador(CompradorId id) {
        return jpa.findById(id.valor()).map(CompradorMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Comprador> todos() {
        return jpa.findAll().stream().map(CompradorMapper::aDominio).toList();
    }
}
