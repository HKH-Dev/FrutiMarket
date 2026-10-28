package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompraEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CompraJpaRepository extends JpaRepository<CompraEntity, UUID> {

    List<CompraEntity> findByCompradorIdOrderByFechaDesc(UUID compradorId);

    /**
     * Ranking: une cada linea de compra con su lote (JOIN) y suma lo vendido por producto (GROUP BY).
     * El detalle solo guarda el id del lote, por eso el JOIN se hace con ON sobre ese id.
     */
    @Query("""
            select new com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.ProductoVendidoFila(
                       l.productoId, l.nombreProducto, l.unidad, sum(d.cantidad), count(distinct c.id))
            from CompraEntity c
            join c.detalles d
            join LoteEntity l on l.id = d.loteId
            where c.estado = :estado
            group by l.productoId, l.nombreProducto, l.unidad
            order by sum(d.cantidad) desc, l.nombreProducto asc
            """)
    List<ProductoVendidoFila> rankingProductosVendidos(@Param("estado") EstadoCompra estado, Limit limite);
}
