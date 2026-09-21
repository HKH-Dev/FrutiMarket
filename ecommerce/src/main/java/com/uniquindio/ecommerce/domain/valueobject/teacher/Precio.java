package com.uniquindio.ecommerce.domain.valueobject.teacher;

import com.uniquindio.ecommerce.domain.exception.ReglaNegocioException;

public record Precio(double monto, String moneda) {
    public Precio {
        if (monto < 0) {
            throw new ReglaNegocioException("El precio no puede ser negativo");
        }
    }

    public Precio conLicencia(Licencia licencia) {
        return new Precio(monto * licencia.factorPrecio(), moneda);
    }
}
