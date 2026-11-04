package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.ProductoMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.ProductoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductoRepositoryJpa implements ProductoRepository {

    private final ProductoJpaRepository jpa;

    public ProductoRepositoryJpa(ProductoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(Producto producto) {
        jpa.save(ProductoMapper.aEntidad(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProducto(ProductoId id) {
        return jpa.findById(id.valor()).map(ProductoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> productosActivos() {
        return jpa.findByActivoTrue().stream().map(ProductoMapper::aDominio).toList();
    }
}
