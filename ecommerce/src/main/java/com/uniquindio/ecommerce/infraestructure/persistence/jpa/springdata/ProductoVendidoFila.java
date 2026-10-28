package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;

import java.math.BigDecimal;
import java.util.UUID;

/** Fila del ranking de productos vendidos, tal como sale de la consulta JPQL. */
public record ProductoVendidoFila(UUID productoId, String nombre, UnidadMedida unidad, BigDecimal cantidadVendida,
                                  Long numeroCompras) {
}
