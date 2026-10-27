package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompraEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.DetalleCompraEmbeddable;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.CompraJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        CompraEntity e = new CompraEntity();
        e.setId(compra.getId().valor());
        e.setCompradorId(compra.getCompradorId().valor());
        e.setFecha(compra.getFecha());
        e.setEstado(compra.getEstado());
        e.setDetalles(new ArrayList<>(compra.getDetalles().stream().map(d -> new DetalleCompraEmbeddable(
                d.getId(), d.getLote().valor(), d.getCantidad().valor(), d.getCantidad().unidad(),
                d.getPrecioUnitario().valorPorUnidad())).toList()));
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> obtenerCompra(PedidoId id) {
        return jpa.findById(id.valor()).map(CompraRepositoryJpa::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> comprasDelComprador(CompradorId comprador) {
        return jpa.findByCompradorIdOrderByFechaDesc(comprador.valor()).stream().map(CompraRepositoryJpa::aDominio).toList();
    }

    private static Compra aDominio(CompraEntity e) {
        List<DetalleCompra> detalles = e.getDetalles().stream().map(d -> DetalleCompra.reconstituir(
                d.getDetalleId(), LoteId.de(d.getLoteId()), Cantidad.de(d.getCantidad(), d.getUnidad()),
                PrecioFinca.de(d.getPrecioUnitario(), d.getUnidad()))).toList();
        return Compra.reconstituir(PedidoId.de(e.getId()), CompradorId.de(e.getCompradorId()), e.getFecha(),
                e.getEstado(), detalles);
    }
}
