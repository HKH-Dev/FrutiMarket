package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.domain.repository.Pagina;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

/** Caso de uso Buscar en el catalogo (comprador): lotes publicados, paginados, con busqueda y filtro. */
public interface BuscarCatalogoLotesUseCase {

    Pagina<VistaLote> buscar(String texto, TipoCultivo tipoCultivo, int pagina, int tamano);
}