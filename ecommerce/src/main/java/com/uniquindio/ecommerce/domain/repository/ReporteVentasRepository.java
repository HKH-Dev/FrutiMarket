package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.math.BigDecimal;
import java.util.List;

/** Consultas de lectura sobre las ventas, que cruzan compras y lotes. */
public interface ReporteVentasRepository {

    /**
     * Ranking de productos por cantidad vendida en compras completadas, de mayor a menor.
     * En empate, por nombre del producto.
     */
    List<ProductoVendido> productosMasVendidos(int limite);

    record ProductoVendido(ProductoId producto, String nombre, BigDecimal cantidadVendida, UnidadMedida unidad,
                           long numeroCompras) {
    }
}
