package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface PagoJpaRepository extends JpaRepository<PagoEntity, UUID> {
    Optional<PagoEntity> findByCompraId(UUID compraId);

    long countByEstado(String estado);

    @Query("""
            select new com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.FilaResumenPagos(
                       coalesce(sum(p.total), 0), coalesce(sum(p.descuentoTemporada + p.descuentoCupon), 0), count(p))
            from PagoEntity p where p.estado = 'APROBADO'
            """)
    FilaResumenPagos resumenAprobados();
}
