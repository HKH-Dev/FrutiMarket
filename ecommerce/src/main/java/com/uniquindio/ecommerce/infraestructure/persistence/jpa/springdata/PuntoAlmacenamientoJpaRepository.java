package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.PuntoAlmacenamientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PuntoAlmacenamientoJpaRepository extends JpaRepository<PuntoAlmacenamientoEntity, UUID> {

    /** El municipio debe llegar normalizado (minusculas y sin espacios extremos). */
    @Query("select distinct a from PuntoAlmacenamientoEntity a join a.municipiosCobertura m where m = :municipio")
    List<PuntoAlmacenamientoEntity> findQueCubren(@Param("municipio") String municipio);
}
