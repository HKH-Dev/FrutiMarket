package com.uniquindio.ecommerce.domain.service;

import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;


import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Regla 16 — los lotes con menor vida util restante tienen prioridad de despacho
 * frente a lotes mas recientes del mismo tipo de cultivo (FEFO, <i>first expired,
 * first out</i>).
 *
 * <p><b>Servicio de dominio, no metodo del agregado.</b> Esta es la excepcion que
 * confirma la regla: las otras diecisiete reglas se cumplen mirando un solo lote,
 * pero esta compara <i>varios lotes entre si</i>. Una regla que necesita mas de una
 * instancia del agregado no cabe dentro de ninguna de ellas sin obligar a un lote a
 * conocer a los demas, que es justo lo que la frontera del agregado prohibe.</p>
 *
 * <p>La clase no tiene estado ni dependencias: recibe los lotes, los ordena y los
 * devuelve. Quien los busca en la base de datos es el caso de uso.</p>
 */
public final class PoliticaFefo {

    private PoliticaFefo() {
    }

    /**
     * Ordena los lotes por prioridad de despacho: primero los que vencen antes.
     * Como criterio de desempate usa la fecha de cosecha, para que el orden sea
     * estable y reproducible.
     */
    public static List<com.uniquindio.ecommerce.domain.catalogo.Lote> ordenarPorPrioridad(List<Lote> lotes, Instant momento) {
        Objects.requireNonNull(lotes, "La lista de lotes no puede ser nula.");
        Objects.requireNonNull(momento, "El momento de evaluacion no puede ser nulo.");
        return lotes.stream()
                .sorted(comparadorFefo(momento))
                .toList();
    }

    /**
     * Aplica la regla 16 dentro de un mismo tipo de cultivo, que es como la enuncia
     * el documento: la prioridad se compara entre lotes comparables, no entre una
     * mora y un bulto de papa.
     */
    public static List<Lote> ordenarPorPrioridad(List<Lote> lotes, TipoCultivo tipoCultivo, Instant momento) {
        Objects.requireNonNull(tipoCultivo, "El tipo de cultivo no puede ser nulo.");
        return lotes.stream()
                .filter(lote -> lote.tipoCultivo() == tipoCultivo)
                .sorted(comparadorFefo(momento))
                .toList();
    }

    /**
     * Comprueba si un despacho respeta la regla 16: no se puede sacar un lote
     * mientras exista otro del mismo cultivo, igualmente despachable, que venza antes.
     */
    public static boolean respetaPrioridad(Lote loteAElegir, List<Lote> candidatos, Instant momento) {
        Objects.requireNonNull(loteAElegir, "El lote elegido no puede ser nulo.");
        return candidatos.stream()
                .filter(otro -> !otro.id().equals(loteAElegir.id()))
                .filter(otro -> otro.tipoCultivo() == loteAElegir.tipoCultivo())
                .noneMatch(otro -> otro.tieneMasPrioridadDeDespachoQue(loteAElegir, momento));
    }

    /** Lote que debe despacharse primero, si hay alguno. */
    public static java.util.Optional<Lote> siguienteADespachar(List<Lote> lotes, Instant momento) {
        return lotes.stream().min(comparadorFefo(momento));
    }

    private static Comparator<Lote> comparadorFefo(Instant momento) {
        return Comparator
                .comparingLong((Lote lote) -> lote.vidaUtilRestante(momento).diasRestantes())
                .thenComparing(Lote::fechaCosecha)
                .thenComparing(lote -> lote.id().valor());
    }
}
