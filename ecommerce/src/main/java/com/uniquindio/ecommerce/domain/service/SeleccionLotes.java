package com.uniquindio.ecommerce.domain.service;

import com.uniquindio.ecommerce.domain.catalogo.Lote;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de dominio: reglas que comparan varios lotes entre si, y que por eso no
 * caben dentro de ningun lote.
 * <ul>
 *   <li><b>Para el comprador:</b> el mejor lote es el de mayor puntaje de calidad;
 *       a igual calidad, el mas fresco; a igual frescura, el mas barato.</li>
 *   <li><b>Regla 16 (FEFO):</b> para despachar, primero el que vence antes.</li>
 * </ul>
 */
public final class SeleccionLotes {

    private SeleccionLotes() {
    }

    public static List<Lote> mejoresPrimero(List<Lote> lotes, Instant momento) {
        return lotes.stream().sorted(comparadorComprador(momento)).toList();
    }

    public static Optional<Lote> mejorLote(List<Lote> lotes, Instant momento) {
        return lotes.stream().min(comparadorComprador(momento));
    }

    public static List<Lote> ordenDeDespacho(List<Lote> lotes, Instant momento) {
        return lotes.stream().sorted(comparadorFefo(momento)).toList();
    }

    /** Regla 16: no se despacha un lote mientras otro del mismo cultivo venza antes. */
    public static boolean respetaPrioridadDeDespacho(Lote loteAElegir, List<Lote> candidatos, Instant momento) {
        return candidatos.stream()
                .filter(otro -> !otro.equals(loteAElegir))
                .filter(otro -> otro.tipoCultivo() == loteAElegir.tipoCultivo())
                .noneMatch(otro -> otro.tieneMasPrioridadDeDespachoQue(loteAElegir, momento));
    }

    private static Comparator<Lote> comparadorComprador(Instant momento) {
        return Comparator
                .comparingInt(Lote::puntajeCalidad).reversed()
                .thenComparing(Lote::fechaCosecha, Comparator.reverseOrder())
                .thenComparing(lote -> lote.precioFinca().valorPorUnidad());
    }

    private static Comparator<Lote> comparadorFefo(Instant momento) {
        return Comparator
                .comparingLong((Lote lote) -> lote.diasParaVencer(momento))
                .thenComparing(Lote::fechaCosecha)
                .thenComparing(lote -> lote.id().valor());
    }
}
