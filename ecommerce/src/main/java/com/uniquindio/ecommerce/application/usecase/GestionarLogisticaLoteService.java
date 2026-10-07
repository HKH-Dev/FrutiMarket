package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarLogisticaLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.CentroRedistribucionPort;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.application.usecase.ServicioDeAplicacion;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.DespachoNoPermitidoException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.service.PoliticaFefo;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.CondicionConservacion;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class GestionarLogisticaLoteService extends ServicioDeAplicacion implements GestionarLogisticaLoteUseCase {

    private static final ZoneId ZONA_OPERACION = ZoneId.of("America/Bogota");

    private final CentroRedistribucionPort centroRedistribucion;

    public GestionarLogisticaLoteService(LoteRepository loteRepository,
                                         Reloj reloj,
                                         PublicadorEventos publicadorEventos,
                                         CentroRedistribucionPort centroRedistribucion) {
        super(loteRepository, reloj, publicadorEventos);
        this.centroRedistribucion = Objects.requireNonNull(centroRedistribucion,
                "El puerto de centros de redistribucion es obligatorio.");
    }

    @Override
    public void declararCondicionConservacion(LoteId lote, CondicionConservacion condicion) {
        ejecutarSobre(lote, agregado -> agregado.declararCondicionConservacion(condicion, reloj.ahora()));
    }

    @Override
    public void enviarAPuntoAcopio(LoteId lote, PuntoAcopioId puntoAcopio) {
        ejecutarSobre(lote, agregado -> agregado.enviarAPuntoAcopio(puntoAcopio, reloj.ahora()));
    }

    @Override
    public void despacharACentro(DespacharLoteCommand comando) {
        Objects.requireNonNull(comando, "El comando de despacho es obligatorio.");
        Instant ahora = reloj.ahora();
        Lote lote = cargar(comando.lote());

        // Regla 13: el centro debe estar habilitado para el tipo de cultivo del lote.
        if (!centroRedistribucion.estaHabilitadoParaCultivo(comando.centroDestino(), lote.tipoCultivo())) {
            throw new DespachoNoPermitidoException("R13",
                    "el centro de redistribucion " + centroRedistribucion.nombreDe(comando.centroDestino())
                            + " no esta habilitado para recibir " + lote.tipoCultivo().etiqueta() + ".");
        }

        // Regla 14: la llegada estimada debe caer dentro de la ventana de entrega.
        if (comando.ventanaEntrega() != null) {
            LocalDateTime llegadaEstimada = LocalDateTime.ofInstant(
                    ahora.plus(comando.tiempoTransito().duracion()), ZONA_OPERACION);
            comando.ventanaEntrega().exigirQueContenga(llegadaEstimada);
        }

        // Regla 16: no se saca un lote si otro del mismo cultivo vence antes.
        lote.puntoAcopioActual().ifPresent(punto -> {
            List<Lote> candidatos = loteRepository.buscarEnAcopioPorCultivo(punto, lote.tipoCultivo());
            if (!PoliticaFefo.respetaPrioridad(lote, candidatos, ahora)) {
                Lote prioritario = PoliticaFefo.siguienteADespachar(candidatos, ahora).orElse(lote);
                throw new DespachoNoPermitidoException("R16",
                        "existe otro lote del mismo cultivo con menor vida util restante ("
                                + prioritario.codigo() + ") que debe despacharse primero.");
            }
        });

        // Reglas 10, 11, 12 y 17: las valida el propio agregado.
        lote.despachar(centroRedistribucion.nombreDe(comando.centroDestino()),
                comando.empaque(), comando.tiempoTransito(), ahora);
        persistirYPublicar(lote);
    }

    @Override
    public void confirmarEntrega(ConfirmarEntregaCommand comando) {
        Objects.requireNonNull(comando, "El comando de entrega es obligatorio.");
        // Regla 15: la direccion del destinatario debe estar en una zona de cobertura habilitada.
        if (!centroRedistribucion.cubreDireccion(comando.centroOrigen(), comando.direccionEntrega())) {
            throw new DespachoNoPermitidoException("R15",
                    "la direccion del destinatario esta fuera de la zona de cobertura del centro "
                            + centroRedistribucion.nombreDe(comando.centroOrigen()) + ".");
        }
        ejecutarSobre(comando.lote(),
                lote -> lote.confirmarEntrega(comando.destinatario(), reloj.ahora()));
    }
}