package com.uniquindio.ecommerce.domain.event;



import com.uniquindio.ecommerce.domain.catalogo.MotivoRevision;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;

/** El lote quedo detenido para revision manual (reglas 11, 17 o 18). */
public record LotePuestoEnRevision(LoteId lote, MotivoRevision motivo, String detalle, Instant ocurridoEn)
        implements EventoDominio {
}
