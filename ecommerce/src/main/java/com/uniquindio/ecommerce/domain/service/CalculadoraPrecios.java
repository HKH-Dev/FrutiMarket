package com.uniquindio.ecommerce.domain.service;

import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.comercial.Liquidacion;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Regla de precios: en cada linea aplica <b>la mejor</b> campana vigente (no se acumulan entre si)
 * y el cupon se calcula sobre lo que queda.
 */
public final class CalculadoraPrecios {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraPrecios() {
    }

    public record Linea(ProductoId producto, BigDecimal subtotal) {
    }

    public static Liquidacion liquidar(List<Linea> lineas, List<DescuentoTemporada> campanas, Cupon cupon, LocalDate fecha) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentoTemporada = BigDecimal.ZERO;
        for (Linea linea : lineas) {
            subtotal = subtotal.add(linea.subtotal());
            descuentoTemporada = descuentoTemporada.add(porcentaje(linea.subtotal(), mejorPorcentaje(linea.producto(), campanas, fecha)));
        }
        BigDecimal base = subtotal.subtract(descuentoTemporada);
        BigDecimal descuentoCupon = cupon == null ? BigDecimal.ZERO : porcentaje(base, cupon.porcentaje());
        return new Liquidacion(escala(subtotal), escala(descuentoTemporada), escala(descuentoCupon),
                escala(base.subtract(descuentoCupon)));
    }

    public static int mejorPorcentaje(ProductoId producto, List<DescuentoTemporada> campanas, LocalDate fecha) {
        return campanas.stream().filter(c -> c.aplicaA(producto, fecha))
                .mapToInt(DescuentoTemporada::porcentaje).max().orElse(0);
    }

    private static BigDecimal porcentaje(BigDecimal valor, int porcentaje) {
        return valor.multiply(BigDecimal.valueOf(porcentaje)).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal escala(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
