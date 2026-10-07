package com.uniquindio.ecommerce.domain.catalogo;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de un lote dentro del marketplace.
 *
 * <p><b>Enum con maquina de estados.</b> Cada constante declara a que estados puede
 * moverse; el agregado {@code Lote} consulta este mapa antes de cualquier cambio,
 * de modo que las transiciones imposibles se detienen en un solo punto del codigo
 * en lugar de repetirse como {@code if} en cada metodo.</p>
 *
 * <p>Los estados salen directamente de las reglas del negocio: {@link #EN_REVISION}
 * existe por las reglas 11 y 18, {@link #DESACTIVADO} por la regla 5 (no se elimina,
 * se desactiva) y {@link #EN_ACOPIO} / {@link #EN_TRANSITO} por las reglas 9 a 13.</p>
 */
public enum EstadoLote {

    /** Registrado pero incompleto: aun no cumple las reglas 2 y 8 para publicarse. */
//    REGISTRADO("Registrado"),

    BORRADOR("Borrador"),

    /** Visible en el catalogo y disponible para pedidos. */
    PUBLICADO("Publicado"),

    /** Sin cantidad disponible; sigue existiendo para trazabilidad y despacho. */
    AGOTADO("Agotado"),

    /** Recibido y consolidado en un punto de acopio (regla 9). */
    EN_ACOPIO("En punto de acopio"),

    /** En traslado con guia de despacho abierta (reglas 10 a 14). */
    EN_TRANSITO("En transito"),

    /** Recibido por el destinatario (regla 15). */
    ENTREGADO("Entregado"),

    /** Detenido por perdida de condiciones o calibre no coincidente (reglas 11, 17, 18). */
    EN_REVISION("En revision"),

    /** Retirado temporalmente de la venta sin borrarse (regla 5). */
    DESACTIVADO("Desactivado"),

    /** Supero su fecha limite de consumo. */
    VENCIDO("Vencido"),

    /** Baja definitiva; solo es posible sin reservas activas (regla 5). */
    RETIRADO("Retirado");

    private final String etiqueta;

    EstadoLote(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    /** Estados a los que este estado puede moverse directamente. */
    public Set<EstadoLote> transicionesPermitidas() {
        return switch (this) {
            case BORRADOR -> EnumSet.of(PUBLICADO, RETIRADO, VENCIDO);
            case PUBLICADO -> EnumSet.of(AGOTADO, EN_ACOPIO, DESACTIVADO, EN_REVISION, VENCIDO, RETIRADO);
            case AGOTADO -> EnumSet.of(PUBLICADO, EN_ACOPIO, EN_REVISION, VENCIDO, RETIRADO);
            case EN_ACOPIO -> EnumSet.of(EN_TRANSITO, EN_REVISION, VENCIDO);
            case EN_TRANSITO -> EnumSet.of(ENTREGADO, EN_REVISION, VENCIDO);
            case EN_REVISION -> EnumSet.of(PUBLICADO, AGOTADO, EN_ACOPIO, EN_TRANSITO, RETIRADO, VENCIDO);
            case DESACTIVADO -> EnumSet.of(PUBLICADO, RETIRADO, VENCIDO);
            case ENTREGADO, VENCIDO, RETIRADO -> EnumSet.noneOf(EstadoLote.class);
        };
    }

    public boolean puedePasarA(EstadoLote destino) {
        return transicionesPermitidas().contains(destino);
    }

    /** Estados en los que el lote admite pedidos nuevos. */
    public boolean admitePedidos() {
        return this == PUBLICADO;
    }

    /** Estados desde los que el lote puede iniciar o continuar un despacho. */
    public boolean permiteDespacho() {
        return this == EN_ACOPIO;
    }

    /** Un estado terminal ya no admite ningun cambio. */
    public boolean esTerminal() {
        return transicionesPermitidas().isEmpty();
    }
}
