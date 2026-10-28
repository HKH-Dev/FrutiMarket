package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository;
import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CompraJpaRepository;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ReporteVentasRepositoryJpa implements ReporteVentasRepository {

    private final CompraJpaRepository jpa;

    public ReporteVentasRepositoryJpa(CompraJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<ProductoVendido> productosMasVendidos(int limite) {
        return jpa.rankingProductosVendidos(EstadoCompra.COMPLETADA, Limit.of(limite)).stream()
                .map(fila -> new ProductoVendido(ProductoId.de(fila.productoId()), fila.nombre(),
                        fila.cantidadVendida(), fila.unidad(), fila.numeroCompras()))
                .toList();
    }
}
