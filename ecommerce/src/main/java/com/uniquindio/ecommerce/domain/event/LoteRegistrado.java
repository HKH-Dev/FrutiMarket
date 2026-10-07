package com.uniquindio.ecommerce.domain.event;



import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;

/** El campesino registro un lote nuevo, todavia en borrador. */
public record LoteRegistrado(LoteId lote, CampesinoId campesino, Cantidad cantidadInicial, Instant ocurridoEn)
        implements EventoDominio {
}
