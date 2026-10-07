package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Cantidad(BigDecimal valor, UnidadMedida unidad) implements Comparable<Cantidad> {

    public Cantidad {
        Objects.requireNonNull(valor, "El valor de la cantidad no puede ser nulo.");
        Objects.requireNonNull(unidad, "La unidad de medida no puede ser nula.");
        if (valor.signum() < 0) {
            throw new ReglaDeNegocioVioladaException("INV-CANTIDAD",
                    "Una cantidad no puede ser negativa: " + valor + " " + unidad.simbolo());
        }
        if (!unidad.admiteFraccion() && valor.stripTrailingZeros().scale() > 0) {
            throw new ReglaDeNegocioVioladaException("INV-CANTIDAD",
                    "La unidad " + unidad.simbolo() + " no admite fracciones: " + valor);
        }
        valor = valor.setScale(unidad.escala(), RoundingMode.HALF_UP);
    }

    public static Cantidad de(String valor, UnidadMedida unidad) {
        return new Cantidad(new BigDecimal(valor), unidad);
    }

    public static Cantidad de(double valor, UnidadMedida unidad) {
        return new Cantidad(BigDecimal.valueOf(valor), unidad);
    }

    public static Cantidad cero(UnidadMedida unidad) {
        return new Cantidad(BigDecimal.ZERO, unidad);
    }

    public Cantidad mas(Cantidad otra) {
        exigirMismaUnidad(otra);
        return new Cantidad(valor.add(otra.valor), unidad);
    }

    public Cantidad menos(Cantidad otra) {
        exigirMismaUnidad(otra);
        BigDecimal resultado = valor.subtract(otra.valor);
        if (resultado.signum() < 0) {
            throw new ReglaDeNegocioVioladaException("INV-CANTIDAD",
                    "La resta deja una cantidad negativa: " + this + " - " + otra);
        }
        return new Cantidad(resultado, unidad);
    }

    /** Devuelve el porcentaje indicado de esta cantidad. Se usa para aplicar la merma. */
    public Cantidad porcentaje(BigDecimal porcentaje) {
        Objects.requireNonNull(porcentaje, "El porcentaje no puede ser nulo.");
        BigDecimal parte = valor.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), unidad.escala(), RoundingMode.HALF_UP);
        return new Cantidad(parte, unidad);
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean esMayorQue(Cantidad otra) {
        exigirMismaUnidad(otra);
        return valor.compareTo(otra.valor) > 0;
    }

    public boolean esMenorQue(Cantidad otra) {
        exigirMismaUnidad(otra);
        return valor.compareTo(otra.valor) < 0;
    }

    @Override
    public int compareTo(Cantidad otra) {
        exigirMismaUnidad(otra);
        return valor.compareTo(otra.valor);
    }

    private void exigirMismaUnidad(Cantidad otra) {
        Objects.requireNonNull(otra, "La cantidad comparada no puede ser nula.");
        if (unidad != otra.unidad) {
            throw new ReglaDeNegocioVioladaException("INV-CANTIDAD",
                    "No se pueden operar cantidades de distinta unidad: "
                            + unidad.simbolo() + " y " + otra.unidad.simbolo());
        }
    }

    @Override
    public String toString() {
        return valor.toPlainString() + " " + unidad.simbolo();
    }
}
