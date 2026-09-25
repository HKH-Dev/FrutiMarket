package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.entity.PuntoAcopio;
import com.uniquindio.ecommerce.domain.valueobject.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;

import java.util.List;
import java.util.Optional;

public interface LoteRepository {

    Lote almacenar(Lote lote);

    Optional<Lote> buscarPorId(String id);

    List<Lote> buscarTodos();

    boolean existeCodigo(String codigo);

    /** Lotes publicados y con cantidad disponible de un producto del catalogo. */
    List<Lote> buscarDisponiblesPorProducto(Producto producto);

    List<Lote> consultarPorCampesino(Campesino campesino);

    List<Lote> buscarPorEstado(EstadoLote estado);

    /** Regla 16: candidatos a despacho desde un punto de acopio, para ordenar por FEFO. */
    List<Lote> buscarEnAcopioPorCultivo(PuntoAcopio puntoAcopio, TipoCultivo tipoCultivo);

    /** Lotes que ya superaron su fecha limite de consumo y siguen activos. */
    List<Lote> buscarVencidosNoCerrados();

    void eliminar(Lote id);

}