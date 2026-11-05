package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.LoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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

    /**
     * Catalogo paginado. Filtra, cuenta y pagina la base de datos (LIMIT/OFFSET), no Java.
     * Parametros con nombre; los filtros opcionales se ignoran cuando llegan en null.
     */
    @Query(value = """
            select l from LoteEntity l
            where l.estado = :estado
              and l.cantidadDisponible > 0
              and (lower(l.nombreProducto) like concat('%', :texto, '%')
                   or lower(l.codigo) like concat('%', :texto, '%'))
              and (:tipoCultivo is null or l.tipoCultivo = :tipoCultivo)
              and (:precioMaximo is null or l.precioFinca <= :precioMaximo)
            """,
            countQuery = """
            select count(l) from LoteEntity l
            where l.estado = :estado
              and l.cantidadDisponible > 0
              and (lower(l.nombreProducto) like concat('%', :texto, '%')
                   or lower(l.codigo) like concat('%', :texto, '%'))
              and (:tipoCultivo is null or l.tipoCultivo = :tipoCultivo)
              and (:precioMaximo is null or l.precioFinca <= :precioMaximo)
            """)
    Page<LoteEntity> buscarDisponibles(@Param("estado") EstadoLote estado,
                                       @Param("texto") String texto,
                                       @Param("tipoCultivo") TipoCultivo tipoCultivo,
                                       @Param("precioMaximo") BigDecimal precioMaximo,
                                       Pageable pagina);

    List<LoteEntity> findByEstadoAndAlmacenActualIdAndTipoCultivo(EstadoLote estado, UUID almacenId, TipoCultivo tipoCultivo);

    List<LoteEntity> findByFechaLimiteConsumoBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoLote> estados);
}
