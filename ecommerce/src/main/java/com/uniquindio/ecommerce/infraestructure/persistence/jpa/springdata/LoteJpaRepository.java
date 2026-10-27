package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

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

    List<LoteEntity> findByEstadoAndAlmacenActualIdAndTipoCultivo(EstadoLote estado, UUID almacenId, TipoCultivo tipoCultivo);

    List<LoteEntity> findByFechaLimiteConsumoBeforeAndEstadoIn(LocalDate fecha, Collection<EstadoLote> estados);
}
