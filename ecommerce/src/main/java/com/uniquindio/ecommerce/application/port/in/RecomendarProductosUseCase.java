package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.List;

public interface RecomendarProductosUseCase {

    /** @param comprador null para un visitante: recibe lo popular, con descuento o en cosecha. */
    List<Sugerencia> recomendar(CompradorId comprador, int limite);

    record Sugerencia(String motivo, int descuento, VistaLote mejorLote) {
    }
}
