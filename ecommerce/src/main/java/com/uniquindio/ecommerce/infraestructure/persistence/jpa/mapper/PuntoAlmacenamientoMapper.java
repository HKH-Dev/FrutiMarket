package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.PuntoAlmacenamientoEntity;

import java.util.HashSet;
import java.util.stream.Collectors;

/** Convierte entre {@link PuntoAlmacenamiento} y su fila con las tablas hijas de cobertura, cultivos, conservaciones y lotes. */
public final class PuntoAlmacenamientoMapper {

    private PuntoAlmacenamientoMapper() {
    }

    public static PuntoAlmacenamientoEntity aEntidad(PuntoAlmacenamiento almacen) {
        PuntoAlmacenamientoEntity e = new PuntoAlmacenamientoEntity();
        e.setId(almacen.id().valor());
        e.setNombre(almacen.nombre());
        e.setTipo(almacen.tipo());
        e.setMunicipio(almacen.municipio());
        e.setCapacidadLotes(almacen.capacidadLotes());
        e.setMunicipiosCobertura(new HashSet<>(almacen.municipiosCobertura()));
        e.setCultivosHabilitados(new HashSet<>(almacen.cultivosHabilitados()));
        e.setConservacionesDisponibles(new HashSet<>(almacen.conservacionesDisponibles()));
        e.setLotesAlmacenados(almacen.lotesAlmacenados().stream().map(LoteId::valor).collect(Collectors.toSet()));
        return e;
    }

    public static PuntoAlmacenamiento aDominio(PuntoAlmacenamientoEntity e) {
        return PuntoAlmacenamiento.builder()
                .id(AlmacenId.de(e.getId()))
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .municipio(e.getMunicipio())
                .capacidadLotes(e.getCapacidadLotes())
                .municipiosCobertura(new HashSet<>(e.getMunicipiosCobertura()))
                .cultivosHabilitados(new HashSet<>(e.getCultivosHabilitados()))
                .conservacionesDisponibles(new HashSet<>(e.getConservacionesDisponibles()))
                .lotesAlmacenados(e.getLotesAlmacenados().stream().map(LoteId::de).collect(Collectors.toSet()))
                .build();
    }
}
