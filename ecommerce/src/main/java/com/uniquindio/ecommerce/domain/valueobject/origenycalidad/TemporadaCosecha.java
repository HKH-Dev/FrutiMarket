package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;

/**
 * Value Object: periodo del ano en que un producto se cosecha. Es un patron anual,
 * por eso usa {@link MonthDay}. Admite temporadas que cruzan el fin de ano.
 */
public final class TemporadaCosecha {

    private final MonthDay inicio;
    private final MonthDay fin;
    private final boolean todoElAnio;

    private TemporadaCosecha(MonthDay inicio, MonthDay fin, boolean todoElAnio) {
        this.inicio = inicio;
        this.fin = fin;
        this.todoElAnio = todoElAnio;
    }

    public static TemporadaCosecha de(MonthDay inicio, MonthDay fin) {
        ReglaDeNegocioVioladaException.validar(inicio != null && fin != null, "INV-TEMPORADA",
                "Una temporada debe tener inicio y fin.");
        ReglaDeNegocioVioladaException.validar(!inicio.equals(fin), "INV-TEMPORADA",
                "El inicio y el fin de la temporada no pueden ser el mismo dia.");
        return new TemporadaCosecha(inicio, fin, false);
    }

    public static TemporadaCosecha permanente() {
        return new TemporadaCosecha(null, null, true);
    }

    public boolean contiene(LocalDate fecha) {
        if (todoElAnio) {
            return true;
        }
        MonthDay dia = MonthDay.from(fecha);
        if (inicio.isAfter(fin)) {
            return !dia.isBefore(inicio) || !dia.isAfter(fin);
        }
        return !dia.isBefore(inicio) && !dia.isAfter(fin);
    }

    public boolean esTodoElAnio() {
        return todoElAnio;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TemporadaCosecha otra && todoElAnio == otra.todoElAnio
                && Objects.equals(inicio, otra.inicio) && Objects.equals(fin, otra.fin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inicio, fin, todoElAnio);
    }

    @Override
    public String toString() {
        return todoElAnio ? "Todo el ano" : inicio + " a " + fin;
    }
}
