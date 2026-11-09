package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.math.BigDecimal;
import java.util.Optional;

public interface PagoRepository {

    void guardar(Pago pago);

    Optional<Pago> deLaCompra(PedidoId compra);

    /** Totales de los pagos aprobados y cantidad de reembolsados. */
    ResumenPagos resumen();

    record ResumenPagos(BigDecimal ingresos, BigDecimal descuentos, long aprobados, long reembolsados) {
    }
}
