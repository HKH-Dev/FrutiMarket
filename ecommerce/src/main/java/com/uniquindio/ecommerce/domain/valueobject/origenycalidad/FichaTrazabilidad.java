package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;


import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.FincaOrigenId;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Registro que documenta el origen completo del lote.
 *
 * <p><b>Value Object (record).</b> Aplicando las tres pruebas del modelado:
 * <ol>
 *   <li>¿Necesita identidad propia? No: se identifica por su contenido.</li>
 *   <li>¿Cambia con el tiempo conservando su identidad? No: una ficha corregida es
 *       una ficha nueva; la anterior no debe poder mutar, porque es justamente la
 *       promesa de transparencia que el comprador esta comprando.</li>
 *   <li>¿Puede existir sin el lote? No: pertenece a su frontera.</li>
 * </ol>
 *
 * <p>Es la pieza central de la regla 8: todo lote debe tener identificado su origen
 * y el campesino responsable antes de ser publicado.</p>
 */
public record FichaTrazabilidad(CampesinoId campesinoResponsable,
                                FincaOrigenId fincaOrigen,
                                TecnicaProduccion tecnica,
                                LocalDate fechaCosechaOElaboracion,
                                String observaciones) {

    public FichaTrazabilidad {
        Objects.requireNonNull(campesinoResponsable,
                "La ficha de trazabilidad exige un campesino responsable (regla 8).");
        Objects.requireNonNull(fincaOrigen,
                "La ficha de trazabilidad exige la finca de origen (regla 8).");
        Objects.requireNonNull(tecnica, "La ficha de trazabilidad exige la tecnica de produccion.");
        Objects.requireNonNull(fechaCosechaOElaboracion,
                "La ficha de trazabilidad exige la fecha de cosecha o elaboracion.");
        observaciones = (observaciones == null || observaciones.isBlank()) ? "" : observaciones.trim();
    }

    public static FichaTrazabilidad registrar(CampesinoId campesino,
                                              FincaOrigenId finca,
                                              TecnicaProduccion tecnica,
                                              LocalDate fecha) {
        return new FichaTrazabilidad(campesino, finca, tecnica, fecha, "");
    }

    /**
     * Regla 8: la ficha esta completa cuando lleva origen, responsable, tecnica y
     * fecha. Los tres primeros los garantiza el constructor; aqui se comprueba la
     * coherencia temporal contra el reloj del caso de uso.
     */
    public boolean estaCompleta(LocalDate hoy) {
        return !fechaCosechaOElaboracion.isAfter(hoy);
    }

    public void validarContra(LocalDate hoy) {
        if (!estaCompleta(hoy)) {
            throw new ReglaDeNegocioVioladaException("R8",
                    "La fecha de cosecha o elaboracion (" + fechaCosechaOElaboracion
                            + ") no puede ser futura.");
        }
    }
}
