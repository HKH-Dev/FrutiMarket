package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Franja de tiempo establecida para realizar la entrega de un pedido.
 * Value Object (record). Soporta la regla 14.
 */
public record VentanaEntrega(LocalDateTime inicio, LocalDateTime fin) {

    public VentanaEntrega {
        Objects.requireNonNull(inicio, "La ventana de entrega debe tener inicio.");
        Objects.requireNonNull(fin, "La ventana de entrega debe tener fin.");
        if (!fin.isAfter(inicio)) {
            throw new ReglaDeNegocioVioladaException("R14",
                    "El fin de la ventana de entrega debe ser posterior a su inicio.");
        }
    }

    public boolean contiene(LocalDateTime momento) {
        return !momento.isBefore(inicio) && !momento.isAfter(fin);
    }

    /** Regla 14: una entrega solo puede programarse dentro de la ventana definida. */
    public void exigirQueContenga(LocalDateTime momentoProgramado) {
        if (!contiene(momentoProgramado)) {
            throw new ReglaDeNegocioVioladaException("R14",
                    "La entrega programada para " + momentoProgramado
                            + " queda fuera de la ventana de entrega " + inicio + " - " + fin + ".");
        }
    }
}
