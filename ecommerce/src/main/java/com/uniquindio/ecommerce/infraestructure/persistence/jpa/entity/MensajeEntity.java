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
@Table(name = "mensajes", indexes = @Index(name = "ix_mensajes_conversacion", columnList = "conversacion_id, momento"))
@Getter
@Setter
@NoArgsConstructor
public class MensajeEntity {

    @Id
    private UUID id;

    @Column(name = "conversacion_id", nullable = false)
    private UUID conversacionId;

    @Column(nullable = false)
    private UUID autor;

    @Column(nullable = false, length = 1000)
    private String texto;

    @Column(nullable = false)
    private Instant momento;
}
