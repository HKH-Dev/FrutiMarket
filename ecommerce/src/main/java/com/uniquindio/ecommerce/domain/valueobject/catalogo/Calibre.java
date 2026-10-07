package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import java.math.BigDecimal;
import java.util.Objects;

public record Calibre(CategoriaCalibre categoria,
                      BigDecimal pesoMinimoGramos,
                      BigDecimal pesoMaximoGramos) {

    public Calibre {
        Objects.requireNonNull(categoria, "La categoria de calibre no puede ser nula.");
        Objects.requireNonNull(pesoMinimoGramos, "El peso minimo del calibre no puede ser nulo.");
        Objects.requireNonNull(pesoMaximoGramos, "El peso maximo del calibre no puede ser nulo.");
        if (pesoMinimoGramos.signum() <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-CALIBRE",
                    "El peso minimo del calibre debe ser mayor que cero.");
        }
        if (pesoMinimoGramos.compareTo(pesoMaximoGramos) > 0) {
            throw new ReglaDeNegocioVioladaException("INV-CALIBRE",
                    "El peso minimo (" + pesoMinimoGramos + " g) no puede superar al maximo ("
                            + pesoMaximoGramos + " g).");
        }
    }

    public static Calibre de(CategoriaCalibre categoria, String minimoGramos, String maximoGramos) {
        return new Calibre(categoria, new BigDecimal(minimoGramos), new BigDecimal(maximoGramos));
    }

    /** Calibre para productos que no se clasifican por tamano (harinas, mermeladas). */
    public static Calibre noAplica() {
        return new Calibre(CategoriaCalibre.INDUSTRIAL, BigDecimal.ONE, new BigDecimal("999999"));
    }

    /** Regla 18: indica si una medicion real cae dentro del rango declarado. */
    public boolean admite(MedicionCalibre medicion) {
        Objects.requireNonNull(medicion, "La medicion no puede ser nula.");
        BigDecimal peso = medicion.pesoPromedioGramos();
        return peso.compareTo(pesoMinimoGramos) >= 0 && peso.compareTo(pesoMaximoGramos) <= 0;
    }

    @Override
    public String toString() {
        return categoria.etiqueta() + " (" + pesoMinimoGramos + "-" + pesoMaximoGramos + " g)";
    }
}
