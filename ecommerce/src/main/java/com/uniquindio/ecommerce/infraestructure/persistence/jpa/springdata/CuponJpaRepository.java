package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuponJpaRepository extends JpaRepository<CuponEntity, UUID> {
    Optional<CuponEntity> findByCodigo(String codigo);

    List<CuponEntity> findByCompradorIdOrderByVenceElAsc(UUID compradorId);
}
