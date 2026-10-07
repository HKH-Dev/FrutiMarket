package com.uniquindio.ecommerce.domain.valueobject.logistica;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.util.Objects;

/** Punto geografico y su direccion legible. Value Object (record). */
public record Ubicacion(String direccion, String municipio, String departamento,
                        double latitud, double longitud) {

    public Ubicacion {
        if (direccion == null || direccion.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-UBICACION",
                    "La ubicacion debe tener direccion.");
        }
        Objects.requireNonNull(municipio, "La ubicacion debe indicar municipio.");
        if (latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
            throw new ReglaDeNegocioVioladaException("INV-UBICACION",
                    "Las coordenadas estan fuera de rango: " + latitud + ", " + longitud);
        }
        direccion = direccion.trim();
    }

    /** Distancia aproximada en kilometros (formula del haversine). */
    public double distanciaKmHasta(Ubicacion otra) {
        Objects.requireNonNull(otra, "La ubicacion destino no puede ser nula.");
        final double radioTierraKm = 6371.0;
        double dLat = Math.toRadians(otra.latitud - latitud);
        double dLon = Math.toRadians(otra.longitud - longitud);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitud)) * Math.cos(Math.toRadians(otra.latitud))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return radioTierraKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
