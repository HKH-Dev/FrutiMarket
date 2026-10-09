package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;

import java.util.List;
import java.util.Optional;

public interface PuntoAlmacenamientoRepository {

    void registrar(PuntoAlmacenamiento almacen);

    Optional<PuntoAlmacenamiento> obtenerAlmacen(AlmacenId id);

    /** Regla 15: almacenamientos que pueden entregar en un municipio. */
    List<PuntoAlmacenamiento> almacenesQueCubren(String municipio);
}
