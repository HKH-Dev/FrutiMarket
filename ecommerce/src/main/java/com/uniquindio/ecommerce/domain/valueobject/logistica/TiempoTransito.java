package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.time.Duration;

/**
 * Tiempo maximo permitido para transportar un lote perecedero sin poner en riesgo
 * su conservacion. Value Object (record).
 *
 * <p>Es el otro lado de la regla 10: se compara contra la vida util restante.</p>
 */
public record TiempoTransito(Duration duracion) {

    public TiempoTransito {
        if (duracion == null || duracion.isZero() || duracion.isNegative()) {
            throw new ReglaDeNegocioVioladaException("INV-TRANSITO",
                    "El tiempo de transito debe ser una duracion positiva.");
        }
    }

    public static TiempoTransito deHoras(long horas) {
        return new TiempoTransito(Duration.ofHours(horas));
    }

    public static TiempoTransito deDias(long dias) {
        return new TiempoTransito(Duration.ofDays(dias));
    }

    public long enHoras() {
        return duracion.toHours();
    }

    @Override
    public String toString() {
        return duracion.toHours() + " h";
    }
}
