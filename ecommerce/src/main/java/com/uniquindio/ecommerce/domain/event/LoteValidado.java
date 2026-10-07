package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.entity.catalogo.EstadoLote;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;

import java.time.Instant;

/** Un responsable valido el lote y este volvio a su estado anterior (regla 11). */
public record LoteValidado(LoteId lote, String responsable, EstadoLote estadoRestituido, Instant ocurridoEn)
        implements EventoDominio {
}
