package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;

import java.math.BigDecimal;
import java.util.UUID;

/** Fila de las ventas de un campesino, tal como sale de la consulta JPQL. */
public record VentaCampesinoFila(UUID productoId, String nombre, UnidadMedida unidad, BigDecimal cantidadVendida,
                                 BigDecimal ingresos, Long numeroCompras) {
}
