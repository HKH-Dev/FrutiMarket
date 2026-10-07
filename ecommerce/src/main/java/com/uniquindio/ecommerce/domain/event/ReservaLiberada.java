package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.time.Instant;

/** Se anulo un pedido y su cantidad volvio al lote. */
public record ReservaLiberada(LoteId lote, PedidoId pedido, Cantidad cantidadDevuelta, Instant ocurridoEn)
        implements EventoDominio {
}
