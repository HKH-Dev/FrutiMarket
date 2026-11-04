package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.CompraMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CompraJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CompraRepositoryJpa implements CompraRepository {

    private final CompraJpaRepository jpa;

    public CompraRepositoryJpa(CompraJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(Compra compra) {
        jpa.save(CompraMapper.aEntidad(compra));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> obtenerCompra(PedidoId id) {
        return jpa.findById(id.valor()).map(CompraMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> comprasDelComprador(CompradorId comprador) {
        return jpa.findByCompradorIdOrderByFechaDesc(comprador.valor()).stream().map(CompraMapper::aDominio).toList();
    }
}
