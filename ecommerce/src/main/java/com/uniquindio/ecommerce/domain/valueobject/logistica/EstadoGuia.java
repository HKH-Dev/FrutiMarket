package com.uniquindio.ecommerce.domain.valueobject.logistica;

/** Ciclo de vida de una guia de despacho. */
public enum EstadoGuia {
    EMITIDA,
    EN_RUTA,
    CERRADA,
    ANULADA;

    public boolean permiteRegistrarHitos() {
        return this == EMITIDA || this == EN_RUTA;
    }
}
