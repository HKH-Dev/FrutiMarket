package com.uniquindio.ecommerce.domain.event;

import java.time.Instant;

/**
 * Hecho del negocio que un agregado registra y el caso de uso publica despues de guardar.
 * Reune en un solo record los 12 eventos que antes eran clases separadas. Es un record
 * porque solo transporta datos: no tiene reglas que proteger.
 */
public record EventoDominio(Tipo tipo, String agregadoId, String detalle, Instant momento) {

    public enum Tipo {
        LOTE_REGISTRADO,
        LOTE_PUBLICADO,
        MERMA_APLICADA,
        CANTIDAD_RESERVADA,
        RESERVA_LIBERADA,
        LOTE_AGOTADO,
        LOTE_ALMACENADO,
        LOTE_DESPACHADO,
        LOTE_PUESTO_EN_REVISION,
        LOTE_VALIDADO,
        LOTE_ENTREGADO,
        COMPRA_CONFIRMADA
    }
}
