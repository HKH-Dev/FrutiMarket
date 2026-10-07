package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Porcentaje de perdida esperada de un lote, especialmente cuando es perecedero.
 *
 * <p><b>Value Object (record).</b> La merma no descuenta dinero, ajusta la <i>cantidad disponible</i>
 * para que el marketplace no prometa producto que se va a perder en el camino.</p>
 */
public record Merma(BigDecimal porcentaje, String justificacion) {

    private static final BigDecimal MAXIMO_ACEPTABLE = BigDecimal.valueOf(40);

    public Merma {
        Objects.requireNonNull(porcentaje, "El porcentaje de merma no puede ser nulo.");
        if (porcentaje.signum() < 0 || porcentaje.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-MERMA",
                    "La merma debe estar entre 0% y 100%. Recibido: " + porcentaje + "%.");
        }
        if (porcentaje.compareTo(MAXIMO_ACEPTABLE) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-MERMA",
                    "Una merma superior al " + MAXIMO_ACEPTABLE
                            + "% exige revision manual del lote, no puede declararse al registrar.");
        }
        porcentaje = porcentaje.setScale(2, RoundingMode.HALF_UP);
        justificacion = (justificacion == null || justificacion.isBlank()) ? "No declarada" : justificacion.trim();
    }

    public static Merma ninguna() {
        return new Merma(BigDecimal.ZERO, "Sin perdida esperada");
    }

    public static Merma de(String porcentaje, String justificacion) {
        return new Merma(new BigDecimal(porcentaje), justificacion);
    }

    public boolean esNula() {
        return porcentaje.signum() == 0;
    }

    /** Cantidad que se pierde al aplicar esta merma sobre la cantidad indicada. */
    public Cantidad perdidaSobre(Cantidad cantidad) {
        return cantidad.porcentaje(porcentaje);
    }

    /** Cantidad que realmente queda disponible despues de descontar la merma. */
    public Cantidad aplicarA(Cantidad cantidad) {
        return cantidad.menos(perdidaSobre(cantidad));
    }

    @Override
    public String toString() {
        return porcentaje.toPlainString() + "%";
    }
}
