package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "compradores")
@Getter
@Setter
@NoArgsConstructor
public class CompradorEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Comprador.Tipo tipo;

    @Column(nullable = false, length = 80)
    private String municipio;

    @Column(nullable = false)
    private boolean activo;
}
