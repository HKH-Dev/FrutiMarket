package com.uniquindio.ecommerce.application.port.out;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

/**
 * Puerto de salida hacia el agregado {@code Campesino}.
 *
 * <p>Existe por la <b>regla 1</b>: un campesino debe estar registrado y autorizado
 * para publicar. Esa comprobacion no puede vivir dentro de {@code Lote} porque el
 * lote no conoce al campesino, solo guarda su identificador; y tampoco debe cargar
 * el agregado {@code Campesino} completo dentro de la transaccion del lote. La
 * salida limpia es este puerto: el caso de uso pregunta, decide y sigue.</p>
 */
public interface AutorizacionCampesinoPort {

    /** Regla 1: el campesino existe, esta activo y tiene permiso para publicar. */
    boolean estaAutorizadoParaPublicar(CampesinoId campesino);

    /** Regla 5: el campesino es el dueno del lote que intenta modificar. */
    boolean esResponsableDelLote(CampesinoId campesino, CampesinoId responsableDelLote);
}
