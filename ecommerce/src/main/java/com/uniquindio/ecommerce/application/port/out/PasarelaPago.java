package com.uniquindio.ecommerce.application.port.out;

import com.uniquindio.ecommerce.domain.comercial.Pago;

import java.math.BigDecimal;

/** Puerto hacia la pasarela de pagos. Hoy es simulada; una real solo necesita otro adaptador. */
public interface PasarelaPago {

    Resultado autorizar(Pago.Metodo metodo, String ultimosDigitos, BigDecimal monto);

    record Resultado(boolean aprobado, String referencia, String motivo) {
    }
}
