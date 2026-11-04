package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.AutorizarCampesinoUseCase;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AutorizarCampesinoService implements AutorizarCampesinoUseCase {

    private final CampesinoRepository campesinoRepository;

    public AutorizarCampesinoService(CampesinoRepository campesinoRepository) {
        this.campesinoRepository = campesinoRepository;
    }

    @Override
    public void autorizar(CampesinoId campesinoId) {
        Campesino campesino = campesinoRepository.obtenerCampesino(campesinoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Campesino", campesinoId));
        campesino.autorizarParaPublicar();
        campesinoRepository.registrar(campesino);
    }
}
