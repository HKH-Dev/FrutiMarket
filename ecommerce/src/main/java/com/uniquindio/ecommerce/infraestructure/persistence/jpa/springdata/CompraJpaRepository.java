package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompraJpaRepository extends JpaRepository<CompraEntity, UUID> {

    List<CompraEntity> findByCompradorIdOrderByFechaDesc(UUID compradorId);
}
