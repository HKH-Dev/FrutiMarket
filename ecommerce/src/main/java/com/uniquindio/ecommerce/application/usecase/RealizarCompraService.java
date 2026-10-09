package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.RealizarCompraUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Compra;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.entity.DetalleCompra;
import com.uniquindio.ecommerce.domain.event.EventoDominio;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CompraRepository;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;

/**
 * <b>Realizar compra</b>: por cada item reserva en el lote (regla 3) con el id de la compra
 * como pedido, confirma la salida y agrega el detalle; al final confirma la compra.
 * Al integrarlo con JPA debe ser {@code @Transactional}: si un item falla, todo se deshace.
 */
public class RealizarCompraService extends ServicioDeAplicacion implements RealizarCompraUseCase {

    private final CompraRepository compraRepository;
    private final CompradorRepository compradorRepository;

    public RealizarCompraService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos,
                                 CompraRepository compraRepository, CompradorRepository compradorRepository) {
        super(loteRepository, reloj, publicadorEventos);
        this.compraRepository = compraRepository;
        this.compradorRepository = compradorRepository;
    }

    @Override
    public CompraResponse comprar(RealizarCompraRequest request) {
        Instant ahora = reloj.ahora();
        CompradorId compradorId = CompradorId.de(request.compradorId());
        Comprador comprador = compradorRepository.obtenerComprador(compradorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comprador", compradorId));
        ReglaDeNegocioVioladaException.validar(comprador.estaActivo(), "INV-COMPRADOR",
                "El comprador " + comprador.getNombre() + " esta inactivo.");

        Compra compra = Compra.builder().compradorId(compradorId).fecha(ahora).build();
        for (RealizarCompraRequest.Item item : request.items()) {
            Lote lote = cargar(LoteId.de(item.loteId()));
            Cantidad cantidad = Cantidad.de(item.cantidad(), lote.cantidadDisponible().unidad());
            lote.reservar(compra.getId(), cantidad, ahora);
            lote.confirmarSalida(compra.getId());
            compra.agregarDetalle(DetalleCompra.crear(lote.id(), cantidad, lote.precioFinca()));
            persistirYPublicar(lote);
        }
        compra.confirmar();
        compraRepository.registrar(compra);
        publicadorEventos.publicar(new EventoDominio(EventoDominio.Tipo.COMPRA_CONFIRMADA,
                compra.getId().toString(), "Total $" + compra.total(), ahora));
        return CompraResponse.desde(compra);
    }
}
