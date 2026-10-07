package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

import com.uniquindio.eccommerce.dominio.exception.ReglaDeNegocioVioladaException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Certificacion que reconoce que el producto procede de una region geografica
 * determinada y posee caracteristicas asociadas a esa region.
 *
 * <p><b>Value Object (record).</b> Se distingue del sello de origen en que no
 * certifica <i>como</i> se produjo sino <i>donde</i>; por eso lleva region y no
 * tecnica, y por eso puede faltar sin que el lote deje de ser publicable.</p>
 */
public record DenominacionOrigen(String nombre,
                                 String regionGeografica,
                                 String entidadReconocedora,
                                 LocalDate fechaVencimiento) {

    public DenominacionOrigen {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-DO",
                    "La denominacion de origen debe tener nombre.");
        }
        if (regionGeografica == null || regionGeografica.isBlank()) {
            throw new ReglaDeNegocioVioladaException("INV-DO",
                    "La denominacion de origen debe indicar la region geografica que ampara.");
        }
        Objects.requireNonNull(fechaVencimiento,
                "La denominacion de origen debe tener fecha de vencimiento.");
        nombre = nombre.trim();
        regionGeografica = regionGeografica.trim();
        entidadReconocedora = (entidadReconocedora == null || entidadReconocedora.isBlank())
                ? "No registrada" : entidadReconocedora.trim();
    }

    /** Regla 17: tambien aplica a la denominacion de origen vencida. */
    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isAfter(fechaVencimiento);
    }
}
