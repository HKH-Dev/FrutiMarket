package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.ProductoEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.ProductoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.MonthDay;
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
        jpa.save(aEntidad(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerProducto(ProductoId id) {
        return jpa.findById(id.valor()).map(ProductoRepositoryJpa::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> productosActivos() {
        return jpa.findByActivoTrue().stream().map(ProductoRepositoryJpa::aDominio).toList();
    }

    static ProductoEntity aEntidad(Producto producto) {
        ProductoEntity e = new ProductoEntity();
        e.setId(producto.id().valor());
        e.setNombre(producto.nombre());
        e.setLinea(producto.linea());
        e.setTipoCultivo(producto.tipoCultivo());
        e.setUnidadVenta(producto.unidadVenta());
        e.setTemporadaInicio(producto.temporada().inicio().map(MonthDay::toString).orElse(null));
        e.setTemporadaFin(producto.temporada().fin().map(MonthDay::toString).orElse(null));
        e.setConservacion(producto.conservacion());
        e.setVidaUtilDias(producto.vidaUtilDias());
        e.setActivo(producto.estaActivo());
        return e;
    }

    static Producto aDominio(ProductoEntity e) {
        Producto producto = Producto.builder()
                .id(ProductoId.de(e.getId()))
                .nombre(e.getNombre())
                .linea(e.getLinea())
                .tipoCultivo(e.getTipoCultivo())
                .unidadVenta(e.getUnidadVenta())
                .temporada(temporada(e.getTemporadaInicio(), e.getTemporadaFin()))
                .conservacion(e.getConservacion())
                .vidaUtilDias(e.getVidaUtilDias())
                .build();
        if (!e.isActivo()) {
            producto.desactivar();
        }
        return producto;
    }

    static TemporadaCosecha temporada(String inicio, String fin) {
        return inicio == null ? TemporadaCosecha.permanente() : TemporadaCosecha.de(MonthDay.parse(inicio), MonthDay.parse(fin));
    }
}
