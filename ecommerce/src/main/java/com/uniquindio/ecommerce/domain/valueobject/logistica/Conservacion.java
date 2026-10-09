package com.uniquindio.ecommerce.domain.valueobject.logistica;

import java.math.BigDecimal;

/**
 * Como debe conservarse un producto. Reune en un solo enum lo que antes eran la
 * condicion de conservacion, los rangos de temperatura y humedad, el tipo de empaque
 * y la cadena de frio: al campesino solo se le pide elegir una de tres opciones.
 */
public enum Conservacion {

    AMBIENTE("Ambiente", new BigDecimal("10"), new BigDecimal("30"), false),
    REFRIGERADO("Refrigerado", new BigDecimal("2"), new BigDecimal("8"), true),
    CONGELADO("Congelado", new BigDecimal("-25"), new BigDecimal("-15"), true);

    private final String etiqueta;
    private final BigDecimal temperaturaMinimaC;
    private final BigDecimal temperaturaMaximaC;
    private final boolean requiereCadenaFrio;

    Conservacion(String etiqueta, BigDecimal temperaturaMinimaC, BigDecimal temperaturaMaximaC, boolean requiereCadenaFrio) {
        this.etiqueta = etiqueta;
        this.temperaturaMinimaC = temperaturaMinimaC;
        this.temperaturaMaximaC = temperaturaMaximaC;
        this.requiereCadenaFrio = requiereCadenaFrio;
    }

    /** Regla 11: una lectura fuera de este rango rompe la cadena de frio. */
    public boolean admiteTemperatura(BigDecimal lecturaC) {
        return lecturaC.compareTo(temperaturaMinimaC) >= 0 && lecturaC.compareTo(temperaturaMaximaC) <= 0;
    }

    public boolean requiereCadenaFrio() {
        return requiereCadenaFrio;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public String rango() {
        return temperaturaMinimaC + " a " + temperaturaMaximaC + " C";
    }
}
