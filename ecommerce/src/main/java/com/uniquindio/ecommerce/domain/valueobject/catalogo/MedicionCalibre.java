package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Medicion real tomada sobre una muestra del lote, para contrastarla contra el
 * calibre declarado (regla 18).
 **/
public record MedicionCalibre(BigDecimal pesoPromedioGramos,
                              int unidadesMuestreadas,
                              LocalDate fechaMedicion,
                              String responsable) {

    public MedicionCalibre {
        Objects.requireNonNull(pesoPromedioGramos, "El peso promedio no puede ser nulo.");
        Objects.requireNonNull(fechaMedicion, "La fecha de medicion no puede ser nula.");
        if (pesoPromedioGramos.signum() <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-CALIBRE",
                    "El peso promedio medido debe ser mayor que cero.");
        }
        if (unidadesMuestreadas < 1) {
            throw new ReglaDeNegocioVioladaException("INV-CALIBRE",
                    "La medicion debe cubrir al menos una unidad muestreada.");
        }
        responsable = (responsable == null || responsable.isBlank()) ? "No registrado" : responsable.trim();
    }

    public static MedicionCalibre de(String pesoPromedioGramos, int unidades, LocalDate fecha, String responsable) {
        return new MedicionCalibre(new BigDecimal(pesoPromedioGramos), unidades, fecha, responsable);
    }
}
