package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

/** Caso de uso <b>Autorizar campesino</b> (administrador): habilita a un campesino para ofrecer lotes (regla 1). */
public interface AutorizarCampesinoUseCase {

    void autorizar(CampesinoId campesino);
}
