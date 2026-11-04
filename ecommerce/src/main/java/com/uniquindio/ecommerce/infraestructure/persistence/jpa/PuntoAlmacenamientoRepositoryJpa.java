package com.uniquindio.ecommerce.infraestructure.persistence.jpa;

import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.repository.PuntoAlmacenamientoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper.PuntoAlmacenamientoMapper;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata.PuntoAlmacenamientoJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@Transactional
public class PuntoAlmacenamientoRepositoryJpa implements PuntoAlmacenamientoRepository {

    private final PuntoAlmacenamientoJpaRepository jpa;

    public PuntoAlmacenamientoRepositoryJpa(PuntoAlmacenamientoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(PuntoAlmacenamiento almacen) {
        jpa.save(PuntoAlmacenamientoMapper.aEntidad(almacen));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PuntoAlmacenamiento> obtenerAlmacen(AlmacenId id) {
        return jpa.findById(id.valor()).map(PuntoAlmacenamientoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuntoAlmacenamiento> almacenesQueCubren(String municipio) {
        if (municipio == null || municipio.isBlank()) {
            return List.of();
        }
        return jpa.findQueCubren(municipio.trim().toLowerCase(Locale.ROOT)).stream()
                .map(PuntoAlmacenamientoMapper::aDominio).toList();
    }
}
