package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;

import java.math.BigDecimal;

/**
 * Control de calidad y de condiciones (reglas 11 y 18). Cada control queda en la cadena
 * de custodia; si algo no cuadra, el lote queda en revision hasta que se valide.
 */
public interface ControlarCalidadLoteUseCase {

    /** Regla 18: devuelve {@code true} si la calidad observada coincide con la declarada. */
    boolean inspeccionarCalidad(LoteId lote, Calidad observada, String inspector);

    /** Regla 11. */
    void registrarTemperatura(LoteId lote, BigDecimal temperaturaC, String responsable);

    void reportarIncidencia(LoteId lote, String detalle, String responsable);

    void validarRevision(LoteId lote, String responsable);

    /** Regla 18: el campesino responsable corrige la calidad que declaro. */
    void corregirCalidadDeclarada(LoteId lote, CampesinoId solicitante, Calidad calidadReal);
}
