package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MensajeJpaRepository extends JpaRepository<MensajeEntity, UUID> {
    List<MensajeEntity> findByConversacionIdOrderByMomentoAsc(UUID conversacionId);
}
