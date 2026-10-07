package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Precio base por unidad que el campesino fija para el lote, antes de cualquier
 * costo de comercializacion.
 *
 * <p><b>Value Object (record).</b> Es el mecanismo con el que el dominio hace
 * visible el valor que realmente recibe el productor: la plataforma puede sumar un
 * margen encima, pero el precio de finca queda registrado sin tocar.</p>
 *
 * <p>Aporta a la regla 2 (un lote no puede publicarse sin precio).</p>
 */
public record PrecioFinca(BigDecimal valorPorUnidad, Moneda moneda, UnidadMedida unidadReferencia) {

    /** Margen maximo que la plataforma puede sumar sobre el precio de finca. */
    private static final BigDecimal MARGEN_MAXIMO_PORCENTAJE = BigDecimal.valueOf(20);

    public PrecioFinca {
        Objects.requireNonNull(valorPorUnidad, "El precio de finca no puede ser nulo.");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula.");
        Objects.requireNonNull(unidadReferencia, "La unidad de referencia del precio no puede ser nula.");
        if (valorPorUnidad.signum() <= 0) {
            throw new ReglaDeNegocioVioladaException("R2",
                    "El precio de finca debe ser mayor que cero. Recibido: " + valorPorUnidad);
        }
        valorPorUnidad = valorPorUnidad.setScale(moneda.escala(), RoundingMode.HALF_UP);
    }

    public static PrecioFinca de(String valor, Moneda moneda, UnidadMedida unidad) {
        return new PrecioFinca(new BigDecimal(valor), moneda, unidad);
    }

    /** Valor total del lote a precio de finca para la cantidad indicada. */
    public BigDecimal totalPara(Cantidad cantidad) {
        Objects.requireNonNull(cantidad, "La cantidad no puede ser nula.");
        if (cantidad.unidad() != unidadReferencia) {
            throw new ReglaDeNegocioVioladaException("INV-PRECIO",
                    "El precio esta expresado por " + unidadReferencia.simbolo()
                            + " y la cantidad viene en " + cantidad.unidad().simbolo() + ".");
        }
        return valorPorUnidad.multiply(cantidad.valor())
                .setScale(moneda.escala(), RoundingMode.HALF_UP);
    }

    /**
     * Precio de venta tras aplicar el margen de la plataforma. Rechaza margenes por
     * encima del tope: es la barrera concreta contra la especulacion que el negocio
     * busca eliminar.
     */
    public BigDecimal conMargenDePlataforma(BigDecimal porcentajeMargen) {
        Objects.requireNonNull(porcentajeMargen, "El margen no puede ser nulo.");
        if (porcentajeMargen.signum() < 0 || porcentajeMargen.compareTo(MARGEN_MAXIMO_PORCENTAJE) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-MARGEN",
                    "El margen de plataforma debe estar entre 0% y "
                            + MARGEN_MAXIMO_PORCENTAJE + "%. Recibido: " + porcentajeMargen + "%.");
        }
        BigDecimal factor = BigDecimal.ONE.add(
                porcentajeMargen.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return valorPorUnidad.multiply(factor).setScale(moneda.escala(), RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return moneda.simbolo() + valorPorUnidad.toPlainString() + "/" + unidadReferencia.simbolo();
    }
}
