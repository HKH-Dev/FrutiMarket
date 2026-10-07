package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.time.Instant;

/** El lote quedo visible en el catalogo y admite pedidos (reglas 2 y 8 cumplidas). */
public record LotePublicado(LoteId lote, ProductoId producto, Cantidad cantidadDisponible, Instant ocurridoEn)
        implements EventoDominio {
}
