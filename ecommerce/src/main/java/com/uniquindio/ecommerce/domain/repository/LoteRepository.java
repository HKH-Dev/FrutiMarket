package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.util.List;
import java.util.Optional;

public interface LoteRepository {

    Lote almacenar(Lote lote);

    Optional<Lote> buscarPorId(LoteId id);

    List<Lote> buscarTodos();

    boolean existeCodigo(String codigo);

    /** Lotes publicados y con cantidad disponible de un producto del catalogo. */
    List<Lote> buscarDisponiblesPorProducto(ProductoId producto);

    List<Lote> consultarPorCampesino(CampesinoId campesino);

    List<Lote> buscarPorEstado(EstadoLote estado);

    /** Regla 16: candidatos a despacho desde un punto de acopio, para ordenar por FEFO. */
    List<Lote> buscarEnAcopioPorCultivo(PuntoAcopioId puntoAcopio, TipoCultivo tipoCultivo);

    /** Lotes que ya superaron su fecha limite de consumo y no estan en un estado terminal. */
    List<Lote> buscarVencidosNoCerrados();

    void eliminar(Lote lote);

}