package com.uniquindio.ecommerce.application.usecase;


import com.uniquindio.ecommerce.application.port.in.GestionarDisponibilidadUseCase;
import com.uniquindio.ecommerce.application.port.out.AutorizacionCampesinoPort;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

import java.util.Objects;

public class GestionarDisponibilidadService extends ServicioDeAplicacion
        implements GestionarDisponibilidadUseCase {

    private final AutorizacionCampesinoPort autorizacionCampesino;

    public GestionarDisponibilidadService(LoteRepository loteRepository,
                                          Reloj reloj,
                                          PublicadorEventos publicadorEventos,
                                          AutorizacionCampesinoPort autorizacionCampesino) {
        super(loteRepository, reloj, publicadorEventos);
        this.autorizacionCampesino = Objects.requireNonNull(autorizacionCampesino,
                "El puerto de autorizacion de campesinos es obligatorio.");
    }

    @Override
    public void reservar(ReservarCantidadCommand comando) {
        Objects.requireNonNull(comando, "El comando de reserva es obligatorio.");
        ejecutarSobre(comando.lote(),
                lote -> lote.reservar(comando.pedido(), comando.cantidad(), reloj.ahora()));
    }

    @Override
    public void liberar(LoteId lote, PedidoId pedido) {
        ejecutarSobre(lote, agregado -> agregado.liberarReserva(pedido, reloj.ahora()));
    }

    @Override
    public void confirmarSalida(LoteId lote, PedidoId pedido) {
        ejecutarSobre(lote, agregado -> agregado.confirmarSalida(pedido, reloj.ahora()));
    }

    @Override
    public void registrarMerma(RegistrarMermaCommand comando) {
        Objects.requireNonNull(comando, "El comando de merma es obligatorio.");
        Lote lote = cargar(comando.lote());
        if (!autorizacionCampesino.esResponsableDelLote(comando.solicitante(), lote.campesinoResponsable())) {
            throw new ReglaDeNegocioVioladaException("R5",
                    "Solo el campesino responsable puede registrar la merma del lote " + lote.codigo() + ".");
        }
        lote.redefinirMerma(comando.merma(), reloj.ahora());
        lote.aplicarMerma(reloj.ahora());
        persistirYPublicar(lote);
    }

    @Override
    public Cantidad consultarDisponible(LoteId lote) {
        return cargar(lote).cantidadDisponible();
    }
}