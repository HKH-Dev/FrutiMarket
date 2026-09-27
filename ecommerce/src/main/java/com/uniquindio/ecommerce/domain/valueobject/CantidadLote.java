package com.uniquindio.ecommerce.domain.valueobject;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;
import java.math.BigDecimal;

public record CantidadLote(BigDecimal valor) {
    public CantidadLote {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad inicial del lote debe ser mayor a cero.");
        }
    }
}
