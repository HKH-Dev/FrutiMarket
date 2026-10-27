package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleCompraEmbeddable {

    @Column(name = "detalle_id", nullable = false)
    private UUID detalleId;

    @Column(name = "lote_id", nullable = false)
    private UUID loteId;

    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadMedida unidad;

    /** Precio de finca pactado al comprar, en la misma unidad de la cantidad. */
    @Column(name = "precio_unitario", nullable = false, precision = 19)
    private BigDecimal precioUnitario;
}
