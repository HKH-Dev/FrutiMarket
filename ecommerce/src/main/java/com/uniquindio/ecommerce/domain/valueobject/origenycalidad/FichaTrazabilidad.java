package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object: documenta el origen del lote (regla 8). Una ficha corregida es una
 * ficha nueva; nunca muta, porque es la promesa de transparencia al comprador.
 */
public final class FichaTrazabilidad {

    private final CampesinoId campesinoResponsable;
    private final UUID fincaOrigen;
    private final TecnicaProduccion tecnica;
    private final LocalDate fechaCosecha;
    private final String observaciones;

    private FichaTrazabilidad(CampesinoId campesinoResponsable, UUID fincaOrigen, TecnicaProduccion tecnica,
                              LocalDate fechaCosecha, String observaciones) {
        this.campesinoResponsable = campesinoResponsable;
        this.fincaOrigen = fincaOrigen;
        this.tecnica = tecnica;
        this.fechaCosecha = fechaCosecha;
        this.observaciones = observaciones;
    }

    public static FichaTrazabilidad registrar(CampesinoId campesino, UUID finca, TecnicaProduccion tecnica,
                                              LocalDate fechaCosecha, String observaciones) {
        ReglaDeNegocioVioladaException.validar(campesino != null, "R8", "La ficha exige un campesino responsable.");
        ReglaDeNegocioVioladaException.validar(finca != null, "R8", "La ficha exige la finca de origen.");
        ReglaDeNegocioVioladaException.validar(tecnica != null, "R8", "La ficha exige la tecnica de produccion.");
        ReglaDeNegocioVioladaException.validar(fechaCosecha != null, "R8", "La ficha exige la fecha de cosecha.");
        String texto = (observaciones == null || observaciones.isBlank()) ? "" : observaciones.trim();
        return new FichaTrazabilidad(campesino, finca, tecnica, fechaCosecha, texto);
    }

    public static FichaTrazabilidad registrar(CampesinoId campesino, UUID finca, TecnicaProduccion tecnica,
                                              LocalDate fechaCosecha) {
        return registrar(campesino, finca, tecnica, fechaCosecha, "");
    }

    /** Regla 8: completa si la fecha de cosecha no es futura respecto a hoy. */
    public boolean estaCompleta(LocalDate hoy) {
        return !fechaCosecha.isAfter(hoy);
    }

    public void validarContra(LocalDate hoy) {
        ReglaDeNegocioVioladaException.validar(estaCompleta(hoy), "R8",
                "La fecha de cosecha de la ficha (" + fechaCosecha + ") no puede ser futura.");
    }

    public CampesinoId campesinoResponsable() { return campesinoResponsable; }
    public UUID fincaOrigen() { return fincaOrigen; }
    public TecnicaProduccion tecnica() { return tecnica; }
    public LocalDate fechaCosecha() { return fechaCosecha; }
    public String observaciones() { return observaciones; }

    @Override
    public boolean equals(Object o) {
        return o instanceof FichaTrazabilidad otra
                && campesinoResponsable.equals(otra.campesinoResponsable)
                && fincaOrigen.equals(otra.fincaOrigen)
                && tecnica == otra.tecnica
                && fechaCosecha.equals(otra.fechaCosecha)
                && observaciones.equals(otra.observaciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(campesinoResponsable, fincaOrigen, tecnica, fechaCosecha, observaciones);
    }
}
