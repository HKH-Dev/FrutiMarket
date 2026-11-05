package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CompradorEntity;

/** Convierte entre la entidad {@link Comprador} del dominio y {@link CompradorEntity}. */
public final class CompradorMapper {

    private CompradorMapper() {
    }

    public static CompradorEntity aEntidad(Comprador comprador) {
        CompradorEntity e = new CompradorEntity();
        e.setId(comprador.getId().valor());
        e.setNombre(comprador.getNombre());
        e.setTipo(comprador.getTipo());
        e.setMunicipio(comprador.getMunicipio());
        e.setActivo(comprador.estaActivo());
        return e;
    }

    /** Un comprador inactivo es un borrado logico: la fila sigue, con activo = false. */
    public static Comprador aDominio(CompradorEntity e) {
        Comprador comprador = Comprador.builder()
                .id(CompradorId.de(e.getId()))
                .nombre(e.getNombre())
                .tipo(e.getTipo())
                .municipio(e.getMunicipio())
                .build();
        if (!e.isActivo()) {
            comprador.desactivar();
        }
        return comprador;
    }
}
