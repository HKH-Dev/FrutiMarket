package com.uniquindio.ecommerce.domain.valueobject;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;

public record CodigoLote(String codigo) {
    public CodigoLote {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException("El código del lote no puede estar vacío.");
        }
        if (!codigo.startsWith("LOT-")) {
            throw new ReglaNegocioException("El código del lote debe iniciar con el prefijo LOT-.");
        }
    }
}