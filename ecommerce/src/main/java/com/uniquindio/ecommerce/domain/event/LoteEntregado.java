package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.valueobject.identidad.DestinatarioId;
import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;

import java.time.Instant;

/** El destinatario recibio fisicamente el lote (regla 15). */
public record LoteEntregado(LoteId lote, DestinatarioId destinatario, Instant ocurridoEn)
        implements EventoDominio {
}
