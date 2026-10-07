package com.uniquindio.ecommerce.domain.event;

import com.uniquindio.eccommerce.dominio.valueobject.identidad.LoteId;

import java.time.Instant;

/** El lote se quedo sin cantidad disponible. */
public record LoteAgotado(LoteId lote, Instant ocurridoEn) implements EventoDominio {
}
