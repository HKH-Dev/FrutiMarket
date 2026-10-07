package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.util.Objects;

/** Rango de temperatura admisible, en grados Celsius. Value Object (record). */
public record RangoTemperatura(BigDecimal minimaC, BigDecimal maximaC) {

    public RangoTemperatura {
        Objects.requireNonNull(minimaC, "La temperatura minima no puede ser nula.");
        Objects.requireNonNull(maximaC, "La temperatura maxima no puede ser nula.");
        if (minimaC.compareTo(maximaC) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-TEMPERATURA",
                    "La temperatura minima (" + minimaC + " C) no puede superar a la maxima ("
                            + maximaC + " C).");
        }
    }

    public static RangoTemperatura de(String minima, String maxima) {
        return new RangoTemperatura(new BigDecimal(minima), new BigDecimal(maxima));
    }

    /** Rango tipico de refrigeracion para hortofruticolas frescos. */
    public static RangoTemperatura refrigeracion() {
        return de("2", "8");
    }

    /** Rango ambiente: el lote no exige cadena de frio. */
    public static RangoTemperatura ambiente() {
        return de("10", "30");
    }

    public boolean admite(BigDecimal lectura) {
        Objects.requireNonNull(lectura, "La lectura de temperatura no puede ser nula.");
        return lectura.compareTo(minimaC) >= 0 && lectura.compareTo(maximaC) <= 0;
    }

    /** Un rango que exige frio activo se considera cadena de frio. */
    public boolean exigeRefrigeracion() {
        return maximaC.compareTo(BigDecimal.valueOf(10)) < 0;
    }

    @Override
    public String toString() {
        return minimaC + " a " + maximaC + " C";
    }
}
