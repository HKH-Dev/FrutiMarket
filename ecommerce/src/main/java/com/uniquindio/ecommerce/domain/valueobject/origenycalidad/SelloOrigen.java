package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Certificacion asociada al lote que acredita una cualidad verificable de origen.
 *
 * <p><b>Value Object (record).</b> Soporta la regla 17: un lote con certificacion
 * de origen vencida no puede ser despachado mientras no se valide de nuevo.</p>
 */
public record SelloOrigen(TipoSello tipo,
                          String entidadCertificadora,
                          String numeroCertificado,
                          LocalDate fechaEmision,
                          LocalDate fechaVencimiento) {

    public SelloOrigen {
        Objects.requireNonNull(tipo, "El tipo de sello no puede ser nulo.");
        Objects.requireNonNull(fechaEmision, "La fecha de emision del sello no puede ser nula.");
        Objects.requireNonNull(fechaVencimiento, "La fecha de vencimiento del sello no puede ser nula.");
        if (entidadCertificadora == null || entidadCertificadora.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-SELLO",
                    "Un sello de origen debe indicar la entidad certificadora que lo emite.");
        }
        if (!fechaVencimiento.isAfter(fechaEmision)) {
            throw new ReglaDeNegocioVioladaException("INV-SELLO",
                    "El vencimiento del sello (" + fechaVencimiento
                            + ") debe ser posterior a su emision (" + fechaEmision + ").");
        }
        entidadCertificadora = entidadCertificadora.trim();
        numeroCertificado = (numeroCertificado == null || numeroCertificado.isBlank())
                ? "SIN-NUMERO" : numeroCertificado.trim();
    }

    /** Emite el sello aplicando la vigencia estandar de su tipo. */
    public static SelloOrigen emitir(TipoSello tipo, String entidad, String numero, LocalDate fechaEmision) {
        return new SelloOrigen(tipo, entidad, numero, fechaEmision,
                fechaEmision.plusDays(tipo.vigenciaDias()));
    }

    /** Regla 17: se evalua contra la fecha del caso de uso, nunca contra el reloj del sistema. */
    public boolean estaVigente(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha de evaluacion no puede ser nula.");
        return !fecha.isAfter(fechaVencimiento) && !fecha.isBefore(fechaEmision);
    }

    public boolean estaVencido(LocalDate fecha) {
        return !estaVigente(fecha);
    }

    @Override
    public String toString() {
        return tipo.etiqueta() + " - " + entidadCertificadora + " (vence " + fechaVencimiento + ")";
    }
}
