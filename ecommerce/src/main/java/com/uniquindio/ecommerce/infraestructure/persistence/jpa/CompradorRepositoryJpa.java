package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompradorEntity;
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
        CompradorEntity e = new CompradorEntity();
        e.setId(comprador.getId().valor());
        e.setNombre(comprador.getNombre());
        e.setTipo(comprador.getTipo());
        e.setMunicipio(comprador.getMunicipio());
        e.setActivo(comprador.estaActivo());
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Comprador> obtenerComprador(CompradorId id) {
        return jpa.findById(id.valor()).map(e -> {
            Comprador comprador = Comprador.builder()
                    .id(CompradorId.de(e.getId()))
                    .nombre(e.getNombre())
                    .tipo(e.getTipo())
                    .municipio(e.getMunicipio())
                    .build();
            if (!e.isActivo()) {
                comprador.desactivar();
            }
            return comprador;
        });
    }
}
