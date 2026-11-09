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
@Table(name = "pagos", uniqueConstraints = @UniqueConstraint(name = "uq_pagos_compra", columnNames = "compra_id"))
@Getter
@Setter
@NoArgsConstructor
public class PagoEntity {

    @Id
    private UUID id;

    @Column(name = "compra_id", nullable = false)
    private UUID compraId;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(nullable = false, length = 40)
    private String referencia;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "descuento_temporada", nullable = false, precision = 19, scale = 2)
    private BigDecimal descuentoTemporada;

    @Column(name = "descuento_cupon", nullable = false, precision = 19, scale = 2)
    private BigDecimal descuentoCupon;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @Column(length = 30)
    private String cupon;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false, length = 15)
    private String estado;
}
