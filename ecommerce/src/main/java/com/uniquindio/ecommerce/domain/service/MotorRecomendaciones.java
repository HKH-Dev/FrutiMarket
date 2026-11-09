package com.uniquindio.ecommerce.domain.service;

import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Recomendaciones por puntaje, explicables: cada punto tiene un motivo que se le muestra al comprador.
 * Pesa mas lo que el comprador ya senalo (favoritos, compras) que la popularidad general.
 */
public final class MotorRecomendaciones {

    static final int PUNTOS_FAVORITO = 5;
    static final int PUNTOS_COMPRADO = 3;
    static final int PUNTOS_MISMO_TIPO = 2;
    static final int PUNTOS_DESCUENTO = 2;
    static final int PUNTOS_POPULAR = 2;
    static final int PUNTOS_TEMPORADA = 1;

    private MotorRecomendaciones() {
    }

    public record Perfil(Set<ProductoId> favoritos, Set<ProductoId> comprados, Set<TipoCultivo> tiposComprados) {
        public static Perfil vacio() {
            return new Perfil(Set.of(), Set.of(), Set.of());
        }
    }

    public record Recomendacion(Producto producto, int puntaje, String motivo) {
    }

    /**
     * @param candidatos productos con al menos un lote disponible.
     * @param populares  productos mas vendidos.
     * @param descuentos porcentaje de descuento vigente por producto.
     */
    public static List<Recomendacion> recomendar(List<Producto> candidatos, Perfil perfil, Set<ProductoId> populares,
                                                 Map<ProductoId, Integer> descuentos, LocalDate hoy, int limite) {
        return candidatos.stream()
                .map(producto -> evaluar(producto, perfil, populares, descuentos, hoy))
                .sorted(Comparator.comparingInt(Recomendacion::puntaje).reversed()
                        .thenComparing(r -> r.producto().nombre()))
                .limit(limite)
                .toList();
    }

    private static Recomendacion evaluar(Producto producto, Perfil perfil, Set<ProductoId> populares,
                                         Map<ProductoId, Integer> descuentos, LocalDate hoy) {
        int puntaje = 0;
        List<String> motivos = new ArrayList<>();
        if (perfil.favoritos().contains(producto.id())) {
            puntaje += PUNTOS_FAVORITO;
            motivos.add("Está en tus favoritos");
        }
        if (perfil.comprados().contains(producto.id())) {
            puntaje += PUNTOS_COMPRADO;
            motivos.add("Ya lo compraste antes");
        } else if (perfil.tiposComprados().contains(producto.tipoCultivo())) {
            puntaje += PUNTOS_MISMO_TIPO;
            motivos.add("Parecido a lo que compras");
        }
        int descuento = descuentos.getOrDefault(producto.id(), 0);
        if (descuento > 0) {
            puntaje += PUNTOS_DESCUENTO;
            motivos.add(descuento + "% de descuento hoy");
        }
        if (populares.contains(producto.id())) {
            puntaje += PUNTOS_POPULAR;
            motivos.add("De los más vendidos");
        }
        if (!producto.temporada().esTodoElAnio() && producto.estaEnTemporada(hoy)) {
            puntaje += PUNTOS_TEMPORADA;
            motivos.add("En cosecha ahora");
        }
        return new Recomendacion(producto, puntaje, motivos.isEmpty() ? "Disponible ahora" : motivos.get(0));
    }
}
