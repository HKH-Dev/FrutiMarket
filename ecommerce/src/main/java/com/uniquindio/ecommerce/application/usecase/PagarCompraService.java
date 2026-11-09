package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.PagarCompraUseCase;
import com.uniquindio.ecommerce.application.port.in.RealizarCompraUseCase;
import com.uniquindio.ecommerce.application.port.out.PasarelaPago;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.comercial.Liquidacion;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CuponRepository;
import com.uniquindio.ecommerce.domain.repository.DescuentoTemporadaRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.PagoRepository;
import com.uniquindio.ecommerce.domain.service.CalculadoraPrecios;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Checkout: reutiliza {@link RealizarCompraUseCase} (stock y reglas de la compra) y agrega precio final y cobro.
 * Todo corre en una transaccion: si la pasarela rechaza, la excepcion deshace la compra, el stock y el cupon.
 */
@Service
@Transactional
public class PagarCompraService implements PagarCompraUseCase {

    private final RealizarCompraUseCase realizarCompra;
    private final LoteRepository lotes;
    private final DescuentoTemporadaRepository descuentos;
    private final CuponRepository cupones;
    private final PagoRepository pagos;
    private final PasarelaPago pasarela;
    private final Reloj reloj;

    public PagarCompraService(RealizarCompraUseCase realizarCompra, LoteRepository lotes, DescuentoTemporadaRepository descuentos,
                              CuponRepository cupones, PagoRepository pagos, PasarelaPago pasarela, Reloj reloj) {
        this.realizarCompra = realizarCompra;
        this.lotes = lotes;
        this.descuentos = descuentos;
        this.cupones = cupones;
        this.pagos = pagos;
        this.pasarela = pasarela;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public Liquidacion cotizar(CompradorId comprador, List<RealizarCompraRequest.Item> items, String codigoCupon) {
        LocalDate hoy = hoy(reloj.ahora());
        Cupon cupon = cargarCupon(codigoCupon);
        if (cupon != null) {
            cupon.exigirRedimible(comprador, hoy);
        }
        List<CalculadoraPrecios.Linea> lineas = new ArrayList<>();
        for (RealizarCompraRequest.Item item : items) {
            Lote lote = cargarLote(LoteId.de(item.loteId()));
            lineas.add(new CalculadoraPrecios.Linea(lote.producto(), lote.precioFinca().valorPorUnidad().multiply(item.cantidad())));
        }
        return CalculadoraPrecios.liquidar(lineas, descuentos.todos(), cupon, hoy);
    }

    @Override
    public Comprobante pagar(CompradorId comprador, RealizarCompraRequest pedido, String codigoCupon,
                             Pago.Metodo metodo, String ultimosDigitos) {
        Instant ahora = reloj.ahora();
        LocalDate hoy = hoy(ahora);
        ReglaDeNegocioVioladaException.validar(metodo != Pago.Metodo.TARJETA || (ultimosDigitos != null && ultimosDigitos.matches("\\d{4}")),
                "R25", "Para pagar con tarjeta indique sus ultimos 4 digitos.");

        // Antes de comprar: la compra usa el precio del lote y lo que se cobra sale de la misma fuente.
        Liquidacion liquidacion = cotizar(comprador, pedido.items(), codigoCupon);
        CompraResponse compra = realizarCompra.comprar(comprador, pedido);

        Cupon cupon = cargarCupon(codigoCupon);
        if (cupon != null) {
            cupon.redimir(comprador, ahora, hoy);
            cupones.guardar(cupon);
        }

        PasarelaPago.Resultado resultado = pasarela.autorizar(metodo, ultimosDigitos, liquidacion.total());
        ReglaDeNegocioVioladaException.validar(resultado.aprobado(), "R25", "Pago rechazado: " + resultado.motivo());

        Pago pago = Pago.aprobado(PedidoId.de(compra.compraId()), comprador, metodo,
                metodo == Pago.Metodo.TARJETA ? ultimosDigitos : null, resultado.referencia(), liquidacion,
                cupon == null ? null : cupon.codigo(), ahora);
        pagos.guardar(pago);
        return new Comprobante(compra, pago);
    }

    private Cupon cargarCupon(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return cupones.porCodigo(Cupon.normalizar(codigo))
                .orElseThrow(() -> new RecursoNoEncontradoException("Cupon", Cupon.normalizar(codigo)));
    }

    private Lote cargarLote(LoteId id) {
        return lotes.obtenerLote(id).orElseThrow(() -> new RecursoNoEncontradoException("Lote", id));
    }

    private static LocalDate hoy(Instant momento) {
        return LocalDate.ofInstant(momento, ZoneId.systemDefault());
    }
}
