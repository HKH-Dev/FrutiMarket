package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;

import java.time.Instant;

/** El lote llego a un punto de acopio con su condicion de conservacion declarada (regla 9). */
public record LoteEnviadoAAcopio(LoteId lote, PuntoAcopioId puntoAcopio, Instant ocurridoEn)
        implements EventoDominio {
}
