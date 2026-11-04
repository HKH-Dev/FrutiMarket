package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

/** Caso de uso <b>Solicitar reembolso</b> (comprador): reglas 19 y 20, dentro del agregado Compra. */
public interface SolicitarReembolsoUseCase {

    CompraResponse reembolsar(PedidoId compra, CompradorId solicitante);
}
