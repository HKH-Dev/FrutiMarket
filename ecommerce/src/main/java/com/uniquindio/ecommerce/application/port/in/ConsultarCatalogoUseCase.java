package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Conservacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.util.List;

/**
 * Caso de uso <b>Consultar catalogo</b>: los productos que pueden ofrecerse. Con estos ids
 * el frontend registra lotes y consulta los disponibles de cada producto.
 */
public interface ConsultarCatalogoUseCase {

    /** Productos activos, ordenados por nombre. */
    List<VistaProducto> productosActivos();

    VistaProducto producto(ProductoId id);

    /**
     * @param vidaUtilDias  0 si no es perecedero.
     * @param temporada     texto legible, p. ej. "Todo el ano" o "--03-01 a --06-30".
     * @param enTemporada   si hoy puede publicarse un lote de este producto.
     */
    record VistaProducto(ProductoId id,
                         String nombre,
                         Producto.Linea linea,
                         TipoCultivo tipoCultivo,
                         UnidadMedida unidadVenta,
                         Conservacion conservacion,
                         int vidaUtilDias,
                         String temporada,
                         boolean enTemporada) {
    }
}
