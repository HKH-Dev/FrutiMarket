package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarDisponibilidadUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

public class GestionarDisponibilidadService extends ServicioDeAplicacion implements GestionarDisponibilidadUseCase {

    public GestionarDisponibilidadService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        super(loteRepository, reloj, publicadorEventos);
    }

    @Override
    public void reservar(LoteId lote, PedidoId pedido, Cantidad cantidad) {
        ejecutarSobre(lote, agregado -> agregado.reservar(pedido, cantidad, reloj.ahora()));
    }

    @Override
    public void liberar(LoteId lote, PedidoId pedido) {
        ejecutarSobre(lote, agregado -> agregado.liberarReserva(pedido, reloj.ahora()));
    }

    @Override
    public void confirmarSalida(LoteId lote, PedidoId pedido) {
        ejecutarSobre(lote, agregado -> agregado.confirmarSalida(pedido));
    }

    @Override
    public void registrarMerma(LoteId loteId, CampesinoId solicitante, Merma merma) {
        Lote lote = cargarComoResponsable(loteId, solicitante);
        lote.redefinirMerma(merma);
        lote.aplicarMerma(reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public Cantidad consultarDisponible(LoteId lote) {
        return cargar(lote).cantidadDisponible();
    }
}
