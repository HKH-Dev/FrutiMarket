package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;

/** Caso de uso <b>Realizar compra</b>: un comprador compra de uno o varios lotes, de distintos campesinos. */
public interface RealizarCompraUseCase {

    CompraResponse comprar(CompradorId comprador, RealizarCompraRequest request);
}
