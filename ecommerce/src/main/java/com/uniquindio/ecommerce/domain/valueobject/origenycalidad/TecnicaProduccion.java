package com.uniquindio.ecommerce.domain.valueobject.origenycalidad;

/** Tecnica con la que fue producida la materia prima del lote. */
public enum TecnicaProduccion {
    ORGANICA_CERTIFICADA("Organica certificada"),
    AGROECOLOGICA("Agroecologica"),
    TRADICIONAL("Tradicional"),
    HIDROPONIA("Hidroponia"),
    INVERNADERO("Invernadero");

    private final String etiqueta;

    TecnicaProduccion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
