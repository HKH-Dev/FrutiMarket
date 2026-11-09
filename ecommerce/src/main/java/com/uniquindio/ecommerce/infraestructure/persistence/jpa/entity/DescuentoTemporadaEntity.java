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
@Table(name = "descuentos_temporada")
@Getter
@Setter
@NoArgsConstructor
public class DescuentoTemporadaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(name = "producto_id")
    private UUID productoId;

    @Column(nullable = false)
    private int porcentaje;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fin;

    @Column(nullable = false)
    private boolean activo;
}
