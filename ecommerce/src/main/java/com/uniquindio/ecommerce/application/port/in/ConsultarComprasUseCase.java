package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.List;

/** Caso de uso <b>Consultar mis compras</b> (comprador), de la mas reciente a la mas antigua. */
public interface ConsultarComprasUseCase {

    List<CompraResponse> comprasDe(CompradorId comprador);
}
