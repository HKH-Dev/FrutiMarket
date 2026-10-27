package com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
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
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
public class ProductoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Producto.Linea linea;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cultivo", nullable = false, length = 30)
    private TipoCultivo tipoCultivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false, length = 20)
    private UnidadMedida unidadVenta;

    /** Formato MonthDay ("--MM-dd"); nulo si la temporada es todo el anio. */
    @Column(name = "temporada_inicio", length = 7)
    private String temporadaInicio;

    @Column(name = "temporada_fin", length = 7)
    private String temporadaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Conservacion conservacion;

    @Column(name = "vida_util_dias", nullable = false)
    private int vidaUtilDias;

    @Column(nullable = false)
    private boolean activo;
}
