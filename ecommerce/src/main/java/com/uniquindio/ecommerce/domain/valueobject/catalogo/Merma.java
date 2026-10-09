package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object: porcentaje de perdida esperada del lote. Ajusta la cantidad
 * disponible, no el precio. Nunca puede declararse por encima del 40 %.
 */
public final class Merma {

    private static final BigDecimal MAXIMO_ACEPTABLE = BigDecimal.valueOf(40);

    private final BigDecimal porcentaje;
    private final String justificacion;

    private Merma(BigDecimal porcentaje, String justificacion) {
        this.porcentaje = porcentaje;
        this.justificacion = justificacion;
    }

    public static Merma de(BigDecimal porcentaje, String justificacion) {
        ReglaDeNegocioVioladaException.validar(porcentaje != null && porcentaje.signum() >= 0, "INV-MERMA",
                "La merma no puede ser nula ni negativa.");
        ReglaDeNegocioVioladaException.validar(porcentaje.compareTo(MAXIMO_ACEPTABLE) <= 0, "INV-MERMA",
                "Una merma superior al " + MAXIMO_ACEPTABLE + "% exige revision manual del lote.");
        String texto = (justificacion == null || justificacion.isBlank()) ? "No declarada" : justificacion.trim();
        return new Merma(porcentaje.setScale(2, RoundingMode.HALF_UP), texto);
    }

    public static Merma de(String porcentaje, String justificacion) {
        return de(new BigDecimal(porcentaje), justificacion);
    }

    public static Merma ninguna() {
        return de(BigDecimal.ZERO, "Sin perdida esperada");
    }

    public boolean esNula() {
        return porcentaje.signum() == 0;
    }

    public Cantidad perdidaSobre(Cantidad cantidad) {
        return cantidad.porcentaje(porcentaje);
    }

    public BigDecimal porcentaje() {
        return porcentaje;
    }

    public String justificacion() {
        return justificacion;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Merma otra && porcentaje.equals(otra.porcentaje) && justificacion.equals(otra.justificacion);
    }

    @Override
    public int hashCode() {
        return 31 * porcentaje.hashCode() + justificacion.hashCode();
    }

    @Override
    public String toString() {
        return porcentaje.toPlainString() + "%";
    }
}
