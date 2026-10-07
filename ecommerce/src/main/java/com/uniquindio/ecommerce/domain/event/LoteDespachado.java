package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.ecommerce.domain.valueobject.identidad.GuiaDespachoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;

/** El lote salio en transito con una guia de despacho abierta (reglas 10 a 12 y 17 cumplidas). */
public record LoteDespachado(LoteId lote, GuiaDespachoId guia, String destino,
                             long diasVidaUtilAlDespachar, Instant ocurridoEn)
        implements EventoDominio {
}
