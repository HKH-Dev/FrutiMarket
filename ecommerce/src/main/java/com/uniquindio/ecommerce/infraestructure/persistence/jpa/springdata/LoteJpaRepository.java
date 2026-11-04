package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LoteJpaRepository extends JpaRepository<LoteEntity, UUID> {

    boolean existsByCodigoIgnoreCase(String codigo);

    List<LoteEntity> findByProductoIdAndEstadoInAndCantidadDisponibleGreaterThan(
            UUID productoId, Collection<EstadoLote> estados, BigDecimal minimo);

    List<LoteEntity> findByCampesinoId(UUID campesinoId);

    List<LoteEntity> findByEstadoAndAlmacenActualIdAndTipoCultivo(EstadoLote estado, UUID almacenId, TipoCultivo tipoCultivo);

    List<LoteEntity> findByFechaLimiteConsumoBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoLote> estados);
    @Query("""
            select l from LoteEntity l
            where l.estado = :estado
              and l.cantidadDisponible > :minimo
              and (lower(l.nombreProducto) like :patron or lower(l.codigo) like :patron)
              and (:filtrarTipo = false or l.tipoCultivo = :tipoCultivo)
            """)
    Page<LoteEntity> buscarEnCatalogo(@Param("estado") EstadoLote estado, @Param("minimo") BigDecimal minimo,
                                      @Param("patron") String patron, @Param("filtrarTipo") boolean filtrarTipo,
                                      @Param("tipoCultivo") TipoCultivo tipoCultivo, Pageable pageable);
}
