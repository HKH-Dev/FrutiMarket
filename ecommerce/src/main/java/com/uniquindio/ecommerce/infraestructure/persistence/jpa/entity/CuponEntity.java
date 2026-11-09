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
@Table(name = "cupones", uniqueConstraints = @UniqueConstraint(name = "uq_cupones_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class CuponEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(nullable = false)
    private int porcentaje;

    @Column(name = "vence_el", nullable = false)
    private LocalDate venceEl;

    @Column(name = "usado_en")
    private Instant usadoEn;
}
