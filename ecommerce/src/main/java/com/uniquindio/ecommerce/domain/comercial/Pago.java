package com.uniquindio.ecommerce.domain.comercial;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Pago aprobado de una compra. Solo existe si la pasarela lo aprobo: un pago rechazado deshace la compra.
 * De la tarjeta solo se conservan los ultimos 4 digitos.
 */
public final class Pago {

    public enum Metodo { TARJETA, PSE, NEQUI }

    public enum Estado { APROBADO, REEMBOLSADO }

    private final UUID id;
    private final PedidoId compra;
    private final CompradorId comprador;
    private final Metodo metodo;
    private final String ultimosDigitos;
    private final String referencia;
    private final Liquidacion liquidacion;
    private final String cupon;
    private final Instant fecha;
    private Estado estado;

    private Pago(UUID id, PedidoId compra, CompradorId comprador, Metodo metodo, String ultimosDigitos, String referencia,
                 Liquidacion liquidacion, String cupon, Instant fecha, Estado estado) {
        this.id = id;
        this.compra = compra;
        this.comprador = comprador;
        this.metodo = metodo;
        this.ultimosDigitos = ultimosDigitos;
        this.referencia = referencia;
        this.liquidacion = liquidacion;
        this.cupon = cupon;
        this.fecha = fecha;
        this.estado = estado;
    }

    public static Pago aprobado(PedidoId compra, CompradorId comprador, Metodo metodo, String ultimosDigitos,
                                String referencia, Liquidacion liquidacion, String cupon, Instant fecha) {
        return new Pago(UUID.randomUUID(), compra, comprador, metodo, ultimosDigitos, referencia, liquidacion, cupon, fecha, Estado.APROBADO);
    }

    public static Pago reconstituir(UUID id, PedidoId compra, CompradorId comprador, Metodo metodo, String ultimosDigitos,
                                    String referencia, Liquidacion liquidacion, String cupon, Instant fecha, Estado estado) {
        return new Pago(id, compra, comprador, metodo, ultimosDigitos, referencia, liquidacion, cupon, fecha, estado);
    }

    public void reembolsar() {
        ReglaDeNegocioVioladaException.validar(estado == Estado.APROBADO, "INV-PAGO", "Este pago ya fue reembolsado.");
        this.estado = Estado.REEMBOLSADO;
    }

    public UUID id() { return id; }
    public PedidoId compra() { return compra; }
    public CompradorId comprador() { return comprador; }
    public Metodo metodo() { return metodo; }
    public String ultimosDigitos() { return ultimosDigitos; }
    public String referencia() { return referencia; }
    public Liquidacion liquidacion() { return liquidacion; }
    public String cupon() { return cupon; }
    public Instant fecha() { return fecha; }
    public Estado estado() { return estado; }
}
