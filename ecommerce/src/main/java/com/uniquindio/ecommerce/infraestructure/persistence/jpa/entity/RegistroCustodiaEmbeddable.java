package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Un paso de la cadena de custodia. Nunca se corrige: solo se agregan nuevos. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroCustodiaEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistroCustodia.Tipo tipo;

    @Column(name = "almacen_id")
    private UUID almacenId;

    @Column(name = "guia_id")
    private UUID guiaId;

    @Column(nullable = false, length = 200)
    private String lugar;

    @Column(nullable = false, length = 120)
    private String responsable;

    @Column(nullable = false)
    private Instant momento;

    @Column(name = "temperatura_c", precision = 6, scale = 2)
    private BigDecimal temperaturaC;

    @Column(nullable = false, length = 500)
    private String observacion;
}
