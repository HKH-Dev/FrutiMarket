package com.uniquindio.ecommerce.domain.valueobject.catalogo;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Informacion tecnica del proceso aplicado a una materia prima para convertirla en
 * producto transformado.
 *
 * <p><b>Value Object (record).</b> No tiene identidad propia: dos fichas con el
 * mismo proceso, fecha, vida util y lote de origen son la misma ficha. Vive dentro
 * de la frontera del agregado {@code Lote} y por eso es inmutable: corregirla
 * significa emitir una ficha nueva, no mutar la anterior.</p>
 *
 * <p>Nota de modelado: {@code loteMateriaPrimaOrigen} es un <b>identificador</b>,
 * no una referencia al objeto {@code Lote}. Un agregado nunca guarda punteros a
 * otra instancia de agregado; asi se mantiene la frontera transaccional.</p>
 */
public record FichaTransformacion(ProcesoTransformacion proceso,
                                  LocalDate fechaElaboracion,
                                  int vidaUtilDiasDeclarada,
                                  LoteId loteMateriaPrimaOrigen,
                                  String descripcionProceso) {

    public FichaTransformacion {
        Objects.requireNonNull(proceso, "El proceso de transformacion no puede ser nulo.");
        Objects.requireNonNull(fechaElaboracion, "La fecha de elaboracion no puede ser nula.");
        Objects.requireNonNull(loteMateriaPrimaOrigen,
                "Un producto transformado debe declarar el lote de materia prima que lo origino.");
        if (vidaUtilDiasDeclarada <= 0) {
            throw new ReglaDeNegocioVioladaException("INV-FICHA-TRANSF",
                    "La vida util declarada debe ser mayor que cero dias.");
        }
        if (vidaUtilDiasDeclarada > proceso.vidaUtilMaximaDias()) {
            throw new ReglaDeNegocioVioladaException("INV-FICHA-TRANSF",
                    "La vida util declarada (" + vidaUtilDiasDeclarada + " dias) supera el maximo de "
                            + proceso.vidaUtilMaximaDias() + " dias admitido para " + proceso.etiqueta() + ".");
        }
        descripcionProceso = (descripcionProceso == null || descripcionProceso.isBlank())
                ? proceso.etiqueta() : descripcionProceso.trim();
    }

    /**
     * Valida contra el reloj del caso de uso que la elaboracion no sea futura.
     * La fecha se recibe por parametro para no acoplar el dominio a {@code LocalDate.now()}.
     */
    public void validarContra(LocalDate hoy) {
        if (fechaElaboracion.isAfter(hoy)) {
            throw new ReglaDeNegocioVioladaException("INV-FICHA-TRANSF",
                    "La fecha de elaboracion (" + fechaElaboracion + ") no puede ser futura.");
        }
    }

    /** Fecha limite de consumo derivada de la elaboracion y la vida util declarada. */
    public LocalDate fechaLimiteConsumo() {
        return fechaElaboracion.plusDays(vidaUtilDiasDeclarada);
    }
}
