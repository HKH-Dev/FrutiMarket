package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Reserva activa de un pedido sobre un lote. La unidad es la del lote. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaLoteEmbeddable {

    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal cantidad;

    @Column(nullable = false)
    private Instant momento;
}
