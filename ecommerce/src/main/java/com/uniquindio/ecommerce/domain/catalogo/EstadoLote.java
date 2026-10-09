package com.uniquindio.ecommerce.domain.catalogo;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de un lote. Cada estado declara a cuales puede pasar; el {@link Lote}
 * consulta este mapa antes de cualquier cambio, asi las transiciones imposibles se
 * detienen en un solo punto del codigo.
 */
public enum EstadoLote {

    REGISTRADO("Registrado"),
    PUBLICADO("Publicado"),
    AGOTADO("Agotado"),
    EN_ACOPIO("En almacenamiento"),
    EN_TRANSITO("En transito"),
    ENTREGADO("Entregado"),
    EN_REVISION("En revision"),
    DESACTIVADO("Desactivado"),
    VENCIDO("Vencido"),
    RETIRADO("Retirado");

    private final String etiqueta;

    EstadoLote(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public Set<EstadoLote> transicionesPermitidas() {
        return switch (this) {
            case REGISTRADO -> EnumSet.of(PUBLICADO, RETIRADO, VENCIDO);
            case PUBLICADO -> EnumSet.of(AGOTADO, EN_ACOPIO, DESACTIVADO, EN_REVISION, VENCIDO, RETIRADO);
            case AGOTADO -> EnumSet.of(PUBLICADO, EN_ACOPIO, EN_REVISION, VENCIDO, RETIRADO);
            case EN_ACOPIO -> EnumSet.of(EN_TRANSITO, EN_REVISION, VENCIDO);
            case EN_TRANSITO -> EnumSet.of(EN_ACOPIO, ENTREGADO, EN_REVISION, VENCIDO);
            case EN_REVISION -> EnumSet.of(PUBLICADO, AGOTADO, EN_ACOPIO, EN_TRANSITO, RETIRADO, VENCIDO);
            case DESACTIVADO -> EnumSet.of(PUBLICADO, RETIRADO, VENCIDO);
            case ENTREGADO, VENCIDO, RETIRADO -> EnumSet.noneOf(EstadoLote.class);
        };
    }

    public boolean puedePasarA(EstadoLote destino) {
        return transicionesPermitidas().contains(destino);
    }

    public boolean admitePedidos() {
        return this == PUBLICADO;
    }

    public boolean permiteDespacho() {
        return this == EN_ACOPIO;
    }

    public boolean esTerminal() {
        return transicionesPermitidas().isEmpty();
    }
}
