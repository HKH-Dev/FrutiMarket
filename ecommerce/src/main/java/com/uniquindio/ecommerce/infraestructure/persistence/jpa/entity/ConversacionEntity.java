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
@Table(name = "conversaciones", uniqueConstraints = @UniqueConstraint(name = "uq_conversaciones", columnNames = {"comprador_id", "campesino_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ConversacionEntity {

    @Id
    private UUID id;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(name = "campesino_id", nullable = false)
    private UUID campesinoId;

    @Column(nullable = false)
    private Instant creada;
}
