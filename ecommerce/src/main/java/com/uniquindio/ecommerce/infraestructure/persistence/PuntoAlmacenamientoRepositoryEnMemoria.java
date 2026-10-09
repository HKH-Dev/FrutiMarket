package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.repository.PuntoAlmacenamientoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PuntoAlmacenamientoRepositoryEnMemoria implements PuntoAlmacenamientoRepository {

    private final Map<AlmacenId, PuntoAlmacenamiento> almacenes = new HashMap<>();

    @Override
    public void registrar(PuntoAlmacenamiento almacen) {
        almacenes.put(almacen.id(), almacen);
    }

    @Override
    public Optional<PuntoAlmacenamiento> obtenerAlmacen(AlmacenId id) {
        return Optional.ofNullable(almacenes.get(id));
    }

    @Override
    public List<PuntoAlmacenamiento> almacenesQueCubren(String municipio) {
        return almacenes.values().stream().filter(almacen -> almacen.cubre(municipio)).toList();
    }
}
