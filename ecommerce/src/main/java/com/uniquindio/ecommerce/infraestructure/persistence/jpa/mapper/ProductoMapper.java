package com.uniquindio.ecommerce.infraestructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.infraestructure.persistence.jpa.entity.ProductoEntity;
import java.time.MonthDay;

/** Convierte entre el agregado {@link Producto} del dominio y la fila {@link ProductoEntity}. */
public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoEntity aEntidad(Producto producto) {
        ProductoEntity e = new ProductoEntity();
        e.setId(producto.id().valor());
        e.setNombre(producto.nombre());
        e.setLinea(producto.linea());
        e.setTipoCultivo(producto.tipoCultivo());
        e.setUnidadVenta(producto.unidadVenta());
        e.setTemporadaInicio(producto.temporada().inicio().map(MonthDay::toString).orElse(null));
        e.setTemporadaFin(producto.temporada().fin().map(MonthDay::toString).orElse(null));
        e.setConservacion(producto.conservacion());
        e.setVidaUtilDias(producto.vidaUtilDias());
        e.setActivo(producto.estaActivo());
        return e;
    }

    public static Producto aDominio(ProductoEntity e) {
        Producto producto = Producto.builder()
                .id(ProductoId.de(e.getId()))
                .nombre(e.getNombre())
                .linea(e.getLinea())
                .tipoCultivo(e.getTipoCultivo())
                .unidadVenta(e.getUnidadVenta())
                .temporada(temporada(e.getTemporadaInicio(), e.getTemporadaFin()))
                .conservacion(e.getConservacion())
                .vidaUtilDias(e.getVidaUtilDias())
                .build();
        if (!e.isActivo()) {
            producto.desactivar();
        }
        return producto;
    }

    public static TemporadaCosecha temporada(String inicio, String fin) {
        return inicio == null ? TemporadaCosecha.permanente() : TemporadaCosecha.de(MonthDay.parse(inicio), MonthDay.parse(fin));
    }
}
