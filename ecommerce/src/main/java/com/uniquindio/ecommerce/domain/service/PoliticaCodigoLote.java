package com.uniquindio.ecommerce.domain.service;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;

/**
 * Servicio de dominio: el codigo de un lote es unico en toda la plataforma.
 *
 * <p>La regla necesita mirar los demas lotes, asi que no cabe dentro de un {@code Lote}. Usa la
 * interfaz {@link LoteRepository} del propio dominio, no la base de datos directamente.</p>
 */
public final class PoliticaCodigoLote {

    private PoliticaCodigoLote() {
    }

    public static void exigirCodigoLibre(String codigo, LoteRepository lotes) {
        ReglaDeNegocioVioladaException.validar(codigo == null || !lotes.existeCodigo(codigo.trim()), "INV-CODIGO",
                "Ya existe un lote con el codigo '" + codigo + "'.");
    }
}
