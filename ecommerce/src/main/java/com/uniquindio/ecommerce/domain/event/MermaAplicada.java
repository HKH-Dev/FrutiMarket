package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.valueobject.catalogo.Cantidad;
import com.uniquindio.eccommerce.dominio.valueobject.catalogo.Merma;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;

import java.time.Instant;

/** Se desconto del lote la perdida esperada. */
public record MermaAplicada(LoteId lote, Merma merma, Cantidad perdida,
                            Cantidad cantidadResultante, Instant ocurridoEn)
        implements EventoDominio {
}
