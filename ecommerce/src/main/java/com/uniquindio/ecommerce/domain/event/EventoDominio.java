package com.uniquindio.ecommerce.domain.event;



import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;

/**
 * Hecho relevante ocurrido dentro del agregado {@code Lote}.
 *
 * <p><b>Interfaz sellada</b>: la lista de eventos del lote es cerrada, asi que el
 * compilador puede exigir que cualquier {@code switch} sobre eventos los cubra
 * todos. Los eventos son records porque describen algo que ya paso y por tanto es
 * inmutable.</p>
 *
 * <p>Para que sirven en esta arquitectura: el agregado los acumula sin conocer a
 * quien los escucha, y el caso de uso los publica por un puerto de salida despues
 * de guardar. Es el mecanismo con el que reglas que viven <i>fuera</i> del lote
 * (avisar al campesino, recalcular la prioridad de despacho de un centro) se
 * enteran de lo ocurrido sin que el dominio dependa de ellas.</p>
 */
public sealed interface EventoDominio
        permits LoteRegistrado, LotePublicado, MermaAplicada, CantidadReservada,
                ReservaLiberada, LoteAgotado, LoteEnviadoAAcopio, LoteDespachado,
                LotePuestoEnRevision, LoteValidado, LoteEntregado {

    /** Lote al que se refiere el hecho. */
    LoteId lote();

    /** Momento en que ocurrio, tomado del reloj del caso de uso. */
    Instant ocurridoEn();

    /** Nombre corto para trazas y colas de mensajes. */
    default String nombre() {
        return getClass().getSimpleName();
    }
}
