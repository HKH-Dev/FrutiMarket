package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.valueobject.catalogo.Cantidad;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.PedidoId;

import java.time.Instant;

/** Se anulo un pedido y su cantidad volvio al lote. */
public record ReservaLiberada(LoteId lote, PedidoId pedido, Cantidad cantidadDevuelta, Instant ocurridoEn)
        implements EventoDominio {
}
