package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FavoritoJpaRepository extends JpaRepository<FavoritoEntity, UUID> {
    boolean existsByCompradorIdAndProductoId(UUID compradorId, UUID productoId);

    List<FavoritoEntity> findByCompradorId(UUID compradorId);

    @Modifying
    @Query("delete from FavoritoEntity f where f.compradorId = :comprador and f.productoId = :producto")
    void quitar(@Param("comprador") UUID comprador, @Param("producto") UUID producto);
}
