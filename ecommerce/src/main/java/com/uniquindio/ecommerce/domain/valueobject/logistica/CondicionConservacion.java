package com.uniquindio.ecommerce.domain.valueobject.logistica;

import java.util.Objects;

/**
 * Conjunto de condiciones necesarias para mantener adecuadamente un lote.
 *
 * <p><b>Value Object (record).</b> Vive dentro del agregado {@code Lote} y es la
 * pieza que sostiene tres reglas:</p>
 * <ul>
 *   <li><b>Regla 9:</b> un lote perecedero debe tenerla definida antes de enviarse
 *       a un punto de acopio.</li>
 *   <li><b>Regla 11:</b> perder estas condiciones manda el lote a revision.</li>
 *   <li><b>Regla 12:</b> el empaque de salida debe ser compatible con ella.</li>
 * </ul>
 */
public record CondicionConservacion(RangoTemperatura temperatura,
                                    RangoHumedad humedad,
                                    boolean requiereCadenaFrio,
                                    boolean requiereEmpaqueHermetico) {

    public CondicionConservacion {
        Objects.requireNonNull(temperatura, "La condicion de conservacion exige rango de temperatura.");
        Objects.requireNonNull(humedad, "La condicion de conservacion exige rango de humedad.");
    }

    /** Condicion para fresco refrigerado: frutas y hortalizas de hoja. */
    public static CondicionConservacion refrigerada() {
        return new CondicionConservacion(RangoTemperatura.refrigeracion(),
                RangoHumedad.estandar(), true, false);
    }

    /** Condicion ambiente: tuberculos, cereales, harinas. */
    public static CondicionConservacion ambiente() {
        return new CondicionConservacion(RangoTemperatura.ambiente(),
                RangoHumedad.de("40", "70"), false, false);
    }

    /** Condicion para conservas y encurtidos: ambiente pero con empaque hermetico. */
    public static CondicionConservacion hermeticaAmbiente() {
        return new CondicionConservacion(RangoTemperatura.ambiente(),
                RangoHumedad.de("40", "70"), false, true);
    }

    /**
     * Regla 12: un lote no puede salir de un punto de acopio si el empaque no es
     * compatible con su condicion de conservacion.
     */
    public boolean esCompatibleCon(TipoEmpaque empaque) {
        Objects.requireNonNull(empaque, "El tipo de empaque no puede ser nulo.");
        if (requiereCadenaFrio && !empaque.esAislanteTermico()) {
            return false;
        }
        return !requiereEmpaqueHermetico || empaque.esHermetico();
    }

    /** Explica por que un empaque es incompatible, para poder informarlo al campesino. */
    public String motivoIncompatibilidad(TipoEmpaque empaque) {
        if (requiereCadenaFrio && !empaque.esAislanteTermico()) {
            return "el lote requiere cadena de frio (" + temperatura
                    + ") y el empaque " + empaque.etiqueta() + " no es aislante termico";
        }
        if (requiereEmpaqueHermetico && !empaque.esHermetico()) {
            return "el lote requiere empaque hermetico y " + empaque.etiqueta() + " no lo es";
        }
        return "";
    }
}
