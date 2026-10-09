package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value Object: precio por unidad, en pesos colombianos, que fija el campesino.
 * Siempre es mayor que cero (regla 2: un lote no se publica sin precio).
 */
public final class PrecioFinca {

    private static final BigDecimal MARGEN_MAXIMO_PORCENTAJE = BigDecimal.valueOf(20);

    private final BigDecimal valorPorUnidad;
    private final UnidadMedida unidadReferencia;

    private PrecioFinca(BigDecimal valorPorUnidad, UnidadMedida unidadReferencia) {
        this.valorPorUnidad = valorPorUnidad;
        this.unidadReferencia = unidadReferencia;
    }

    public static PrecioFinca de(BigDecimal valorPorUnidad, UnidadMedida unidadReferencia) {
        ReglaDeNegocioVioladaException.validar(valorPorUnidad != null && valorPorUnidad.signum() > 0, "R2",
                "El precio de finca debe ser mayor que cero. Recibido: " + valorPorUnidad);
        ReglaDeNegocioVioladaException.validar(unidadReferencia != null, "R2",
                "El precio debe indicar la unidad a la que se refiere.");
        return new PrecioFinca(valorPorUnidad.setScale(0, RoundingMode.HALF_UP), unidadReferencia);
    }

    public static PrecioFinca de(String valorPorUnidad, UnidadMedida unidadReferencia) {
        return de(new BigDecimal(valorPorUnidad), unidadReferencia);
    }

    /** Valor de la cantidad indicada a precio de finca. */
    public BigDecimal totalPara(Cantidad cantidad) {
        ReglaDeNegocioVioladaException.validar(cantidad.unidad() == unidadReferencia, "INV-PRECIO",
                "El precio esta expresado por " + unidadReferencia.simbolo()
                        + " y la cantidad viene en " + cantidad.unidad().simbolo() + ".");
        return valorPorUnidad.multiply(cantidad.valor()).setScale(0, RoundingMode.HALF_UP);
    }

    /** Precio de venta con el margen de la plataforma; el margen nunca puede superar el 20 %. */
    public BigDecimal conMargenDePlataforma(BigDecimal porcentajeMargen) {
        ReglaDeNegocioVioladaException.validar(porcentajeMargen != null && porcentajeMargen.signum() >= 0
                        && porcentajeMargen.compareTo(MARGEN_MAXIMO_PORCENTAJE) <= 0, "INV-MARGEN",
                "El margen de plataforma debe estar entre 0% y " + MARGEN_MAXIMO_PORCENTAJE + "%.");
        BigDecimal factor = BigDecimal.ONE.add(porcentajeMargen.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return valorPorUnidad.multiply(factor).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal valorPorUnidad() {
        return valorPorUnidad;
    }

    public UnidadMedida unidadReferencia() {
        return unidadReferencia;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PrecioFinca otro && valorPorUnidad.equals(otro.valorPorUnidad)
                && unidadReferencia == otro.unidadReferencia;
    }

    @Override
    public int hashCode() {
        return 31 * valorPorUnidad.hashCode() + unidadReferencia.hashCode();
    }

    @Override
    public String toString() {
        return "$" + valorPorUnidad.toPlainString() + "/" + unidadReferencia.simbolo();
    }
}
