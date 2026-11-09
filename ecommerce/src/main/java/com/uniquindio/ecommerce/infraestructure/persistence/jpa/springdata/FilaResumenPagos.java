package com.uniquindio.ecommerce.infraestructure.persistence.jpa.springdata;

import java.math.BigDecimal;

/** Totales de los pagos aprobados, tal como salen de la consulta JPQL. */
public record FilaResumenPagos(BigDecimal ingresos, BigDecimal descuentos, Long aprobados) {
}
