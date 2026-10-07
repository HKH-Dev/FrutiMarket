package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.util.Objects;

/** Rango de humedad relativa admisible, en porcentaje. Value Object (record). */
public record RangoHumedad(BigDecimal minimaPorcentaje, BigDecimal maximaPorcentaje) {

    public RangoHumedad {
        Objects.requireNonNull(minimaPorcentaje, "La humedad minima no puede ser nula.");
        Objects.requireNonNull(maximaPorcentaje, "La humedad maxima no puede ser nula.");
        if (minimaPorcentaje.signum() < 0 || maximaPorcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-HUMEDAD",
                    "La humedad relativa debe expresarse entre 0% y 100%.");
        }
        if (minimaPorcentaje.compareTo(maximaPorcentaje) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-HUMEDAD",
                    "La humedad minima no puede superar a la maxima.");
        }
    }

    public static RangoHumedad de(String minima, String maxima) {
        return new RangoHumedad(new BigDecimal(minima), new BigDecimal(maxima));
    }

    public static RangoHumedad estandar() {
        return de("60", "90");
    }

    public boolean admite(BigDecimal lectura) {
        return lectura.compareTo(minimaPorcentaje) >= 0 && lectura.compareTo(maximaPorcentaje) <= 0;
    }
}
