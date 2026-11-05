package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.CampesinoEntity;

/** Convierte entre la entidad {@link Campesino} del dominio y {@link CampesinoEntity}. */
public final class CampesinoMapper {

    private CampesinoMapper() {
    }

    public static CampesinoEntity aEntidad(Campesino campesino) {
        CampesinoEntity e = new CampesinoEntity();
        e.setId(campesino.getId().valor());
        e.setNombre(campesino.getNombre());
        e.setNumeroIdentificacion(campesino.getNumeroIdentificacion());
        e.setEscalaProduccion(campesino.getEscalaProduccion());
        e.setAutorizadoParaPublicar(campesino.estaAutorizadoParaPublicar());
        return e;
    }

    /** El estado (autorizado) se restaura con el metodo de negocio, no con un setter. */
    public static Campesino aDominio(CampesinoEntity e) {
        Campesino campesino = Campesino.builder()
                .id(CampesinoId.de(e.getId()))
                .nombre(e.getNombre())
                .numeroIdentificacion(e.getNumeroIdentificacion())
                .escalaProduccion(e.getEscalaProduccion())
                .build();
        if (e.isAutorizadoParaPublicar()) {
            campesino.autorizarParaPublicar();
        }
        return campesino;
    }
}
