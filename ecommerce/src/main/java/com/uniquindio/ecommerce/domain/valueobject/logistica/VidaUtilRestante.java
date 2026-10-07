package com.uniquindio.ecommerce.domain.valueobject.logistica;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Tiempo que le queda a un lote antes de alcanzar su limite de vida util.
 *
 * <p><b>Value Object (record) calculado, no almacenado.</b> El glosario insiste en
 * que no es la vida util original sino <i>lo que queda</i>, y que se recalcula en
 * cada etapa. Por eso el lote guarda la fecha limite de consumo y deriva este
 * objeto cada vez que se le pregunta, pasando la fecha del caso de uso: asi nunca
 * hay un valor obsoleto persistido en base de datos.</p>
 *
 * <p>Soporta las reglas 10 y 16.</p>
 */
public record VidaUtilRestante(long diasRestantes, LocalDate fechaLimite, boolean aplica) {

    /** Lotes no perecederos: la vida util restante no limita el despacho. */
    public static VidaUtilRestante noAplica() {
        return new VidaUtilRestante(Long.MAX_VALUE, null, false);
    }

    public static VidaUtilRestante calcular(LocalDate fechaLimite, LocalDate hoy) {
        Objects.requireNonNull(fechaLimite, "La fecha limite no puede ser nula.");
        Objects.requireNonNull(hoy, "La fecha de evaluacion no puede ser nula.");
        return new VidaUtilRestante(ChronoUnit.DAYS.between(hoy, fechaLimite), fechaLimite, true);
    }

    public boolean estaVencida() {
        return aplica && diasRestantes < 0;
    }

    /**
     * Regla 10: indica si la vida util restante alcanza para cubrir el tiempo de
     * transito previsto. Se exige margen estricto: si el lote llega justo el dia
     * del vencimiento, no se despacha.
     */
    public boolean alcanzaPara(Duration tiempoTransito) {
        Objects.requireNonNull(tiempoTransito, "El tiempo de transito no puede ser nulo.");
        if (!aplica) {
            return true;
        }
        long diasTransito = tiempoTransito.toDays() + (tiempoTransito.toHoursPart() > 0 ? 1 : 0);
        return diasRestantes > diasTransito;
    }

    /** Regla 16: menor vida util restante implica mayor prioridad de despacho (FEFO). */
    public boolean tieneMasPrioridadQue(VidaUtilRestante otra) {
        Objects.requireNonNull(otra, "La vida util comparada no puede ser nula.");
        if (!aplica) {
            return false;
        }
        if (!otra.aplica) {
            return true;
        }
        return diasRestantes < otra.diasRestantes;
    }

    @Override
    public String toString() {
        return aplica ? diasRestantes + " dias (limite " + fechaLimite + ")" : "No perecedero";
    }
}
