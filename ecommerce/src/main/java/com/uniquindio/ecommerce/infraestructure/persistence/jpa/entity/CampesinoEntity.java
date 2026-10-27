package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "campesinos")
@Getter
@Setter
@NoArgsConstructor
public class CampesinoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "numero_identificacion", nullable = false, unique = true, length = 30)
    private String numeroIdentificacion;

    @Column(name = "escala_produccion", nullable = false)
    private double escalaProduccion;

    @Column(name = "autorizado_para_publicar", nullable = false)
    private boolean autorizadoParaPublicar;
}
