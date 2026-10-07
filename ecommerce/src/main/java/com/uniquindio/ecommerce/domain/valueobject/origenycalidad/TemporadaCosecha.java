package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Objects;

/**
 * Periodo del ano durante el cual un producto agricola se encuentra naturalmente
 * disponible.
 *
 * <p><b>Value Object (record).</b> Se modela con {@link MonthDay} y no con fechas
 * completas porque la temporada es un patron anual recurrente, no un intervalo
 * unico: la cosecha de mora va de abril a junio <i>todos los anos</i>.</p>
 *
 * <p>Soporta la regla del catalogo: un lote de materia prima no puede publicarse
 * fuera de la temporada declarada para su producto. Contempla temporadas que
 * cruzan el fin de ano (por ejemplo noviembre a febrero).</p>
 */
public record TemporadaCosecha(MonthDay inicio, MonthDay fin, boolean todoElAnio) {

    public TemporadaCosecha {
        if (!todoElAnio) {
            Objects.requireNonNull(inicio, "Una temporada no permanente debe tener fecha de inicio.");
            Objects.requireNonNull(fin, "Una temporada no permanente debe tener fecha de fin.");
            if (inicio.equals(fin)) {
                throw new ReglaDeNegocioVioladaException("INV-TEMPORADA",
                        "El inicio y el fin de la temporada no pueden ser el mismo dia.");
            }
        }
    }

    public static TemporadaCosecha de(MonthDay inicio, MonthDay fin) {
        return new TemporadaCosecha(inicio, fin, false);
    }

    /** Productos disponibles todo el ano (transformados, cultivos bajo invernadero). */
    public static TemporadaCosecha permanente() {
        return new TemporadaCosecha(null, null, true);
    }

    /**
     * Indica si la fecha cae dentro de la temporada. Resuelve el caso de temporadas
     * que cruzan el cambio de ano invirtiendo la comparacion.
     */
    public boolean contiene(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha evaluada no puede ser nula.");
        if (todoElAnio) {
            return true;
        }
        MonthDay dia = MonthDay.from(fecha);
        boolean cruzaAnio = inicio.isAfter(fin);
        if (cruzaAnio) {
            return !dia.isBefore(inicio) || !dia.isAfter(fin);
        }
        return !dia.isBefore(inicio) && !dia.isAfter(fin);
    }

    @Override
    public String toString() {
        return todoElAnio ? "Todo el ano" : inicio + " a " + fin;
    }
}
