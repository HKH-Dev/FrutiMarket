package com.uniquindio.ecommerce.domain.valueobject.logistica;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Estado de la cadena de frio de un lote a lo largo del almacenamiento y el transporte.
 *
 * <p><b>Value Object (record) pese a representar un estado que cambia.</b> El truco
 * de modelado es que el objeto no muta: cuando llega una lectura nueva, el lote
 * <i>reemplaza</i> su cadena de frio por otra instancia. Asi se conserva la
 * inmutabilidad del Value Object y toda la logica de ruptura queda en un solo sitio.</p>
 *
 * <p>Soporta la regla 11.</p>
 */
public record CadenaDeFrio(boolean requerida,
                           boolean intacta,
                           Instant ultimaVerificacion,
                           BigDecimal ultimaLecturaC,
                           String motivoRuptura) {

    public CadenaDeFrio {
        motivoRuptura = (motivoRuptura == null || motivoRuptura.isBlank()) ? "" : motivoRuptura.trim();
    }

    /** Cadena de frio no aplicable: el lote no es perecedero refrigerado. */
    public static CadenaDeFrio noAplica() {
        return new CadenaDeFrio(false, true, null, null, "");
    }

    /** Inicia la cadena de frio en estado intacto para un lote que la requiere. */
    public static CadenaDeFrio iniciar(Instant momento) {
        return new CadenaDeFrio(true, true, momento, null, "");
    }

    /**
     * Registra una lectura de temperatura. Devuelve una nueva cadena de frio: si la
     * lectura sale del rango exigido, queda marcada como rota.
     */
    public CadenaDeFrio registrarLectura(BigDecimal temperaturaC, RangoTemperatura rangoExigido, Instant momento) {
        Objects.requireNonNull(temperaturaC, "La lectura de temperatura no puede ser nula.");
        Objects.requireNonNull(rangoExigido, "El rango exigido no puede ser nulo.");
        if (!requerida) {
            return this;
        }
        if (!intacta) {
            return new CadenaDeFrio(true, false, momento, temperaturaC, motivoRuptura);
        }
        boolean dentroDeRango = rangoExigido.admite(temperaturaC);
        String motivo = dentroDeRango ? ""
                : "Lectura de " + temperaturaC + " C fuera del rango exigido " + rangoExigido;
        return new CadenaDeFrio(true, dentroDeRango, momento, temperaturaC, motivo);
    }

    /** Marca la ruptura explicitamente, por ejemplo tras un reporte del transportador. */
    public CadenaDeFrio romper(String motivo, Instant momento) {
        return new CadenaDeFrio(requerida, false, momento, ultimaLecturaC, motivo);
    }

    /** Restablece la cadena tras la validacion manual que exige la regla 11. */
    public CadenaDeFrio restablecer(Instant momento) {
        return new CadenaDeFrio(requerida, true, momento, ultimaLecturaC, "");
    }

    public boolean estaRota() {
        return requerida && !intacta;
    }
}
