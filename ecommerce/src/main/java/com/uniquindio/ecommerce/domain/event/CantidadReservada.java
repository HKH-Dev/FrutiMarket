package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.valueobject.catalogo.Cantidad;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.PedidoId;

import java.time.Instant;

/** Un pedido comprometio parte de la cantidad disponible del lote (regla 3). */
public record CantidadReservada(LoteId lote, PedidoId pedido, Cantidad cantidad,
                                Cantidad cantidadRestante, Instant ocurridoEn)
        implements EventoDominio {
}
