package com.uniquindio.ecommerce.domain.valueobject;

public enum EstadoLote {
    REGISTRADO,   // creado, aun no visible en el catalogo
    PUBLICADO,    // visible y vendible
    EN_ACOPIO,    // fisicamente en un punto de acopio, candidato a despacho (regla 16)
    AGOTADO,      // cantidadDisponible == 0
    VENCIDO,      // supero fechaLimiteConsumo
    CERRADO;      // ciclo de vida terminado, no participa en consultas activas

    public boolean esActivo() {
        return this == REGISTRADO || this == PUBLICADO || this == EN_ACOPIO || this == AGOTADO;
    }

    public boolean permiteVenta() {
        return this == PUBLICADO || this == EN_ACOPIO;
    }
}