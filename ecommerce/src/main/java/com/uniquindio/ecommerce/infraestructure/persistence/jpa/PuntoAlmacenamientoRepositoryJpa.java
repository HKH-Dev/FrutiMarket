package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.repository.PuntoAlmacenamientoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.PuntoAlmacenamientoEntity;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.PuntoAlmacenamientoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
public class PuntoAlmacenamientoRepositoryJpa implements PuntoAlmacenamientoRepository {

    private final PuntoAlmacenamientoJpaRepository jpa;

    public PuntoAlmacenamientoRepositoryJpa(PuntoAlmacenamientoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(PuntoAlmacenamiento almacen) {
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
        jpa.save(e);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PuntoAlmacenamiento> obtenerAlmacen(AlmacenId id) {
        return jpa.findById(id.valor()).map(PuntoAlmacenamientoRepositoryJpa::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoAlmacenamiento> almacenesQueCubren(String municipio) {
        if (municipio == null || municipio.isBlank()) {
            return List.of();
        }
        return jpa.findQueCubren(municipio.trim().toLowerCase(Locale.ROOT)).stream()
                .map(PuntoAlmacenamientoRepositoryJpa::aDominio).toList();
    }

    private static PuntoAlmacenamiento aDominio(PuntoAlmacenamientoEntity e) {
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
