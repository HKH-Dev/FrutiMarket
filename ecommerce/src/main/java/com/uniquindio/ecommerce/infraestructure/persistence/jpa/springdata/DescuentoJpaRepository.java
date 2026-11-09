package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DescuentoJpaRepository extends JpaRepository<DescuentoTemporadaEntity, UUID> {
}
