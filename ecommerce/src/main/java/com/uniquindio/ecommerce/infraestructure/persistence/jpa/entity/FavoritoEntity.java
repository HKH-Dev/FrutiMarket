package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(name = "uq_favoritos", columnNames = {"comprador_id", "producto_id"}))
@Getter
@Setter
@NoArgsConstructor
public class FavoritoEntity {

    @Id
    private UUID id;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(nullable = false)
    private Instant agregado;
}
