package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.util.Objects;
import java.util.Set;

/**
 * Area geografica en la que un centro de redistribucion garantiza la entrega.
 * Value Object (record). Soporta la regla 15.
 *
 * <p>Vive en el agregado {@code CentroRedistribucion}, no en {@code Lote}: la
 * cobertura pertenece al centro y sobrevive a cualquier lote concreto.</p>
 */
public record ZonaCobertura(String nombre, Set<String> municipiosCubiertos, double radioKm, Ubicacion centro) {

    public ZonaCobertura {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-ZONA", "La zona de cobertura debe tener nombre.");
        }
        Objects.requireNonNull(municipiosCubiertos, "La zona debe declarar los municipios que cubre.");
        Objects.requireNonNull(centro, "La zona de cobertura debe tener un centro de referencia.");
        if (municipiosCubiertos.isEmpty()) {
            throw new ReglaDeNegocioVioladaException("INV-ZONA",
                    "Una zona de cobertura debe cubrir al menos un municipio.");
        }
        if (radioKm <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-ZONA", "El radio de cobertura debe ser positivo.");
        }
        municipiosCubiertos = Set.copyOf(municipiosCubiertos);
        nombre = nombre.trim();
    }

    /** Regla 15: un destinatario solo recibe entrega directa si su direccion esta cubierta. */
    public boolean cubre(Ubicacion destino) {
        Objects.requireNonNull(destino, "El destino no puede ser nulo.");
        return municipiosCubiertos.contains(destino.municipio())
                && centro.distanciaKmHasta(destino) <= radioKm;
    }
}
