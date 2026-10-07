package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import java.util.Objects;


public record ExcedenteCosecha(Cantidad cantidadNoColocada, String canalTradicionalFallido) {

    public ExcedenteCosecha {
        Objects.requireNonNull(cantidadNoColocada, "La cantidad no colocada no puede ser nula.");
        canalTradicionalFallido = (canalTradicionalFallido == null || canalTradicionalFallido.isBlank())
                ? "No especificado" : canalTradicionalFallido.trim();
    }

    public boolean esRelevante() {
        return !cantidadNoColocada.esCero();
    }
}
