package com.uniquindio.ecommerce.domain.valueobject;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import java.math.BigDecimal;

public record PrecioUnitario(BigDecimal valor) {
    public PrecioUnitario {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio unitario del lote debe ser mayor a cero.");
        }
    }
}
