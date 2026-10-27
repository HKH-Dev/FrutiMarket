package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.valueobject.EstadoCompra;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "compras", indexes = @Index(name = "ix_compras_comprador", columnList = "comprador_id"))
@Getter
@Setter
@NoArgsConstructor
public class CompraEntity {

    @Id
    private UUID id;

    @Column(name = "comprador_id", nullable = false)
    private UUID compradorId;

    @Column(nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "detalles_compra", joinColumns = @JoinColumn(name = "compra_id"))
    private List<DetalleCompraEmbeddable> detalles = new ArrayList<>();
}
