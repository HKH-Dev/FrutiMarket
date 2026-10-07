package com.uniquindio.ecommerce.application.port.out;

import java.time.Instant;

/**
 * Puerto de salida para obtener la hora actual.
 *
 * <p>Ninguna clase del dominio llama a {@code Instant.now()}: el momento siempre
 * entra por parametro. Este puerto es quien lo provee, y en pruebas se sustituye
 * por un reloj fijo, de modo que reglas dependientes del tiempo — vencimiento de
 * vida util (regla 10), temporada de cosecha, vigencia de certificaciones
 * (regla 17) — pueden comprobarse sin esperar a que pase el tiempo real.</p>
 */
@FunctionalInterface
public interface Reloj {

    Instant ahora();

    /** Reloj del sistema, para produccion. */
    static Reloj delSistema() {
        return Instant::now;
    }

    /** Reloj congelado en un instante, para pruebas. */
    static Reloj fijo(Instant momento) {
        return () -> momento;
    }
}
