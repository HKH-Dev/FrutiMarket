package com.uniquindio.ecommerce.domain.valueobject.logistica;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Paso registrado en el recorrido de un lote.
 *
 * <p><b>Value Object (record).</b> Un hito es un hecho ocurrido: una vez anotado
 * no se corrige, se anota otro. Por eso la guia de despacho guarda una lista de
 * hitos inmutables y solo permite anadir al final.</p>
 */
public record HitoDespacho(TipoHito tipo,
                           String ubicacion,
                           Instant momento,
                           BigDecimal temperaturaC,
                           String observacion) {

    public HitoDespacho {
        Objects.requireNonNull(tipo, "El hito debe tener tipo.");
        Objects.requireNonNull(momento, "El hito debe tener momento.");
        ubicacion = (ubicacion == null || ubicacion.isBlank()) ? "No registrada" : ubicacion.trim();
        observacion = (observacion == null) ? "" : observacion.trim();
    }

    public static HitoDespacho de(TipoHito tipo, String ubicacion, Instant momento) {
        return new HitoDespacho(tipo, ubicacion, momento, null, "");
    }

    public static HitoDespacho conTemperatura(TipoHito tipo, String ubicacion, Instant momento, BigDecimal temperaturaC) {
        return new HitoDespacho(tipo, ubicacion, momento, temperaturaC, "");
    }

    public boolean traeLecturaDeTemperatura() {
        return temperaturaC != null;
    }
}
