package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object: cantidad de producto con su unidad de medida.
 * Nunca es negativa, siempre queda normalizada a la escala de su unidad y las
 * unidades discretas (canastilla, bulto, unidad) no admiten fracciones.
 */
public final class Cantidad {

    private final BigDecimal valor;
    private final UnidadMedida unidad;

    private Cantidad(BigDecimal valor, UnidadMedida unidad) {
        this.valor = valor;
        this.unidad = unidad;
    }

    public static Cantidad de(BigDecimal valor, UnidadMedida unidad) {
        ReglaDeNegocioVioladaException.validar(valor != null, "INV-CANTIDAD", "El valor de la cantidad no puede ser nulo.");
        ReglaDeNegocioVioladaException.validar(unidad != null, "INV-CANTIDAD", "La unidad de medida no puede ser nula.");
        ReglaDeNegocioVioladaException.validar(valor.signum() >= 0, "INV-CANTIDAD",
                "Una cantidad no puede ser negativa: " + valor + " " + unidad.simbolo());
        ReglaDeNegocioVioladaException.validar(unidad.admiteFraccion() || valor.stripTrailingZeros().scale() <= 0,
                "INV-CANTIDAD", "La unidad " + unidad.simbolo() + " no admite fracciones: " + valor);
        return new Cantidad(valor.setScale(unidad.escala(), RoundingMode.HALF_UP), unidad);
    }

    public static Cantidad de(String valor, UnidadMedida unidad) {
        return de(new BigDecimal(valor), unidad);
    }

    public static Cantidad cero(UnidadMedida unidad) {
        return de(BigDecimal.ZERO, unidad);
    }

    public Cantidad mas(Cantidad otra) {
        exigirMismaUnidad(otra);
        return de(valor.add(otra.valor), unidad);
    }

    /** Falla si el resultado fuese negativo: el dominio prefiere fallar a guardar un stock imposible. */
    public Cantidad menos(Cantidad otra) {
        exigirMismaUnidad(otra);
        return de(valor.subtract(otra.valor), unidad);
    }

    /** Porcentaje de esta cantidad. Se usa para aplicar la merma. */
    public Cantidad porcentaje(BigDecimal porcentaje) {
        BigDecimal parte = valor.multiply(porcentaje)
                .divide(BigDecimal.valueOf(100), unidad.escala(), RoundingMode.HALF_UP);
        return de(parte, unidad);
    }

    public boolean esCero() {
        return valor.signum() == 0;
    }

    public boolean esMayorQue(Cantidad otra) {
        exigirMismaUnidad(otra);
        return valor.compareTo(otra.valor) > 0;
    }

    private void exigirMismaUnidad(Cantidad otra) {
        ReglaDeNegocioVioladaException.validar(otra != null && unidad == otra.unidad, "INV-CANTIDAD",
                "No se pueden operar cantidades de distinta unidad.");
    }

    public BigDecimal valor() {
        return valor;
    }

    public UnidadMedida unidad() {
        return unidad;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cantidad otra && valor.equals(otra.valor) && unidad == otra.unidad;
    }

    @Override
    public int hashCode() {
        return 31 * valor.hashCode() + unidad.hashCode();
    }

    @Override
    public String toString() {
        return valor.toPlainString() + " " + unidad.simbolo();
    }
}
