package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteRepository {

    Lote almacenar(Lote lote);

    Optional<Lote> obtenerLote(LoteId id);

    boolean existeCodigo(String codigo);

    /** Lotes publicados y con cantidad disponible de un producto, de cualquier campesino. */
    List<Lote> lotesDisponiblesDe(ProductoId producto);

    List<Lote> lotesDelCampesino(CampesinoId campesino);

    /** Regla 16: lotes guardados en un almacenamiento, para ordenarlos por FEFO. */
    List<Lote> lotesEnAlmacen(AlmacenId almacen, TipoCultivo tipoCultivo);

    List<Lote> lotesVencidosSinCerrar(LocalDate hoy);
}
