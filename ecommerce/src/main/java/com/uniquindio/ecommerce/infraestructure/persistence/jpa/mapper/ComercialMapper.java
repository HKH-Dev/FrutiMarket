package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.chat.Conversacion;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.comercial.Liquidacion;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.*;

/** Conversiones entre el dominio comercial/chat y sus entidades JPA. */
public final class ComercialMapper {

    private ComercialMapper() {
    }

    public static DescuentoTemporadaEntity aEntidad(DescuentoTemporada d) {
        DescuentoTemporadaEntity e = new DescuentoTemporadaEntity();
        e.setId(d.id());
        e.setNombre(d.nombre());
        e.setProductoId(d.producto().map(ProductoId::valor).orElse(null));
        e.setPorcentaje(d.porcentaje());
        e.setInicio(d.inicio());
        e.setFin(d.fin());
        e.setActivo(d.activo());
        return e;
    }

    public static DescuentoTemporada aDominio(DescuentoTemporadaEntity e) {
        return DescuentoTemporada.reconstituir(e.getId(), e.getNombre(),
                e.getProductoId() == null ? null : ProductoId.de(e.getProductoId()),
                e.getPorcentaje(), e.getInicio(), e.getFin(), e.isActivo());
    }

    public static CuponEntity aEntidad(Cupon c) {
        CuponEntity e = new CuponEntity();
        e.setId(c.id());
        e.setCodigo(c.codigo());
        e.setCompradorId(c.comprador().valor());
        e.setPorcentaje(c.porcentaje());
        e.setVenceEl(c.venceEl());
        e.setUsadoEn(c.usadoEn().orElse(null));
        return e;
    }

    public static Cupon aDominio(CuponEntity e) {
        return Cupon.reconstituir(e.getId(), e.getCodigo(), CompradorId.de(e.getCompradorId()), e.getPorcentaje(), e.getVenceEl(), e.getUsadoEn());
    }

    public static PagoEntity aEntidad(Pago p) {
        PagoEntity e = new PagoEntity();
        e.setId(p.id());
        e.setCompraId(p.compra().valor());
        e.setCompradorId(p.comprador().valor());
        e.setMetodo(p.metodo().name());
        e.setUltimosDigitos(p.ultimosDigitos());
        e.setReferencia(p.referencia());
        e.setSubtotal(p.liquidacion().subtotal());
        e.setDescuentoTemporada(p.liquidacion().descuentoTemporada());
        e.setDescuentoCupon(p.liquidacion().descuentoCupon());
        e.setTotal(p.liquidacion().total());
        e.setCupon(p.cupon());
        e.setFecha(p.fecha());
        e.setEstado(p.estado().name());
        return e;
    }

    public static Pago aDominio(PagoEntity e) {
        return Pago.reconstituir(e.getId(), PedidoId.de(e.getCompraId()), CompradorId.de(e.getCompradorId()),
                Pago.Metodo.valueOf(e.getMetodo()), e.getUltimosDigitos(), e.getReferencia(),
                new Liquidacion(e.getSubtotal(), e.getDescuentoTemporada(), e.getDescuentoCupon(), e.getTotal()),
                e.getCupon(), e.getFecha(), Pago.Estado.valueOf(e.getEstado()));
    }

    public static ConversacionEntity aEntidad(Conversacion c) {
        ConversacionEntity e = new ConversacionEntity();
        e.setId(c.id());
        e.setCompradorId(c.comprador().valor());
        e.setCampesinoId(c.campesino().valor());
        e.setCreada(c.creada());
        return e;
    }

    public static Conversacion aDominio(ConversacionEntity e) {
        return Conversacion.reconstituir(e.getId(), CompradorId.de(e.getCompradorId()), CampesinoId.de(e.getCampesinoId()), e.getCreada());
    }

    public static MensajeEntity aEntidad(Mensaje m) {
        MensajeEntity e = new MensajeEntity();
        e.setId(m.id());
        e.setConversacionId(m.conversacion());
        e.setAutor(m.autor());
        e.setTexto(m.texto());
        e.setMomento(m.momento());
        return e;
    }

    public static Mensaje aDominio(MensajeEntity e) {
        return new Mensaje(e.getId(), e.getConversacionId(), e.getAutor(), e.getTexto(), e.getMomento());
    }
}
