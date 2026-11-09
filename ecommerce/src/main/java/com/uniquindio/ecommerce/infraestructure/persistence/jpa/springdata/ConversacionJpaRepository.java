package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversacionJpaRepository extends JpaRepository<ConversacionEntity, UUID> {
    Optional<ConversacionEntity> findByCompradorIdAndCampesinoId(UUID compradorId, UUID campesinoId);

    List<ConversacionEntity> findByCompradorIdOrCampesinoId(UUID compradorId, UUID campesinoId);
}
