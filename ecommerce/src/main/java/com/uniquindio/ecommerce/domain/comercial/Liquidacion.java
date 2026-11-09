package com.uniquindio.ecommerce.domain.comercial;

import java.math.BigDecimal;

/** Value Object: como se llega del subtotal al total que se cobra. */
public record Liquidacion(BigDecimal subtotal, BigDecimal descuentoTemporada, BigDecimal descuentoCupon, BigDecimal total) {
}
