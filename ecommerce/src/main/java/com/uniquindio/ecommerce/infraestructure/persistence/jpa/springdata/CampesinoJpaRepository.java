package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CampesinoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CampesinoJpaRepository extends JpaRepository<CampesinoEntity, UUID> {
}
