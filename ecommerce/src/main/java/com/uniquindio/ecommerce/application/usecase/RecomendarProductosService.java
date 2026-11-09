package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.application.port.in.RecomendarProductosUseCase;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.catalogo.Producto;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.repository.DescuentoTemporadaRepository;
import com.uniquindio.ecommerce.domain.repository.FavoritoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.ProductoRepository;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository;
import com.uniquindio.ecommerce.domain.service.CalculadoraPrecios;
import com.uniquindio.ecommerce.domain.service.MotorRecomendaciones;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Reune los datos que el {@link MotorRecomendaciones} necesita; la decision es del dominio. */
@Service
@Transactional(readOnly = true)
public class RecomendarProductosService implements RecomendarProductosUseCase {

    private static final int CUANTOS_POPULARES = 5;

    private final ProductoRepository productos;
    private final LoteRepository lotes;
    private final CompraRepository compras;
    private final FavoritoRepository favoritos;
    private final DescuentoTemporadaRepository descuentos;
    private final ReporteVentasRepository ventas;
    private final ConsultarLoteUseCase consultarLote;
    private final Reloj reloj;

    public RecomendarProductosService(ProductoRepository productos, LoteRepository lotes, CompraRepository compras,
                                      FavoritoRepository favoritos, DescuentoTemporadaRepository descuentos,
                                      ReporteVentasRepository ventas, ConsultarLoteUseCase consultarLote, Reloj reloj) {
        this.productos = productos;
        this.lotes = lotes;
        this.compras = compras;
        this.favoritos = favoritos;
        this.descuentos = descuentos;
        this.ventas = ventas;
        this.consultarLote = consultarLote;
        this.reloj = reloj;
    }

    @Override
    public List<Sugerencia> recomendar(CompradorId comprador, int limite) {
        LocalDate hoy = LocalDate.ofInstant(reloj.ahora(), ZoneId.systemDefault());
        Map<ProductoId, VistaLote> mejores = new LinkedHashMap<>();
        Map<ProductoId, Integer> porcentajes = new HashMap<>();
        List<DescuentoTemporada> campanas = descuentos.todos();
        List<Producto> candidatos = productos.productosActivos().stream().filter(producto -> {
            var mejor = consultarLote.mejorLoteDe(producto.id());
            mejor.ifPresent(lote -> mejores.put(producto.id(), lote));
            porcentajes.put(producto.id(), CalculadoraPrecios.mejorPorcentaje(producto.id(), campanas, hoy));
            return mejor.isPresent();
        }).toList();

        Set<ProductoId> populares = ventas.productosMasVendidos(CUANTOS_POPULARES).stream()
                .map(ReporteVentasRepository.ProductoVendido::producto).collect(Collectors.toSet());

        return MotorRecomendaciones.recomendar(candidatos, perfilDe(comprador), populares, porcentajes, hoy, limite).stream()
                .map(r -> new Sugerencia(r.motivo(), porcentajes.get(r.producto().id()), mejores.get(r.producto().id())))
                .toList();
    }

    private MotorRecomendaciones.Perfil perfilDe(CompradorId comprador) {
        if (comprador == null) {
            return MotorRecomendaciones.Perfil.vacio();
        }
        Set<ProductoId> comprados = new HashSet<>();
        Set<TipoCultivo> tipos = new HashSet<>();
        compras.comprasDelComprador(comprador).stream()
                .flatMap(compra -> compra.getDetalles().stream())
                .map(DetalleCompra::getLote)
                .distinct()
                .map(lotes::obtenerLote)
                .flatMap(java.util.Optional::stream)
                .forEach((Lote lote) -> {
                    comprados.add(lote.producto());
                    tipos.add(lote.tipoCultivo());
                });
        return new MotorRecomendaciones.Perfil(favoritos.delComprador(comprador), comprados, tipos);
    }
}
