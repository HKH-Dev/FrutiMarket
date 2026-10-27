package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompradorJpaRepository extends JpaRepository<CompradorEntity, UUID> {
}
