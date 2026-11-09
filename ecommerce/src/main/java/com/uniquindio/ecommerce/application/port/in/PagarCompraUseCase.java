package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.domain.comercial.Liquidacion;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.List;

public interface PagarCompraUseCase {

    /** Calcula lo que se cobraria, sin guardar nada ni consumir el cupon. */
    Liquidacion cotizar(CompradorId comprador, List<RealizarCompraRequest.Item> items, String cupon);

    /** Compra, cobra y consume el cupon en una transaccion: si el pago se rechaza, no queda nada. */
    Comprobante pagar(CompradorId comprador, RealizarCompraRequest compra, String cupon, Pago.Metodo metodo, String ultimosDigitos);

    record Comprobante(CompraResponse compra, Pago pago) {
    }
}
