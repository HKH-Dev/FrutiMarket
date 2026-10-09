package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarLogisticaLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.entity.PuntoAlmacenamiento;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.repository.PuntoAlmacenamientoRepository;
import com.uniquindio.ecommerce.domain.service.SeleccionLotes;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Logistica del lote. Lo que pertenece al lote (vida util, revision, cadena de frio)
 * lo valida el lote; lo que pertenece al almacenamiento (capacidad, cultivos, cobertura)
 * lo valida el almacenamiento; aqui solo se coordinan y se aplica el FEFO (regla 16).
 * Al integrarlo con JPA debe ser {@code @Transactional}: se modifican dos agregados.
 */
public class GestionarLogisticaLoteService extends ServicioDeAplicacion implements GestionarLogisticaLoteUseCase {

    private final PuntoAlmacenamientoRepository almacenRepository;
    private final CompradorRepository compradorRepository;

    public GestionarLogisticaLoteService(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos,
                                         PuntoAlmacenamientoRepository almacenRepository,
                                         CompradorRepository compradorRepository) {
        super(loteRepository, reloj, publicadorEventos);
        this.almacenRepository = almacenRepository;
        this.compradorRepository = compradorRepository;
    }

    @Override
    public void ingresarAAlmacen(LoteId loteId, AlmacenId almacenId, String responsable) {
        Lote lote = cargar(loteId);
        PuntoAlmacenamiento almacen = cargarAlmacen(almacenId);
        almacen.recibirLote(lote.id(), lote.tipoCultivo(), lote.conservacion());
        lote.ingresarAAlmacen(almacen.id(), almacen.nombre(), responsable, reloj.ahora());
        almacenRepository.registrar(almacen);
        persistirYPublicar(lote);
    }

    @Override
    public void despacharAAlmacen(LoteId loteId, AlmacenId destinoId, Duration tiempoTransito, String responsable) {
        Lote lote = cargar(loteId);
        PuntoAlmacenamiento destino = cargarAlmacen(destinoId);
        ReglaDeNegocioVioladaException.validar(destino.puedeRecibir(lote.tipoCultivo(), lote.conservacion()), "R13",
                destino.nombre() + " no puede recibir este lote (cultivo, conservacion o capacidad).");
        despachar(lote, destino.nombre(), tiempoTransito, responsable);
    }

    @Override
    public void despacharAComprador(LoteId loteId, CompradorId compradorId, Duration tiempoTransito, String responsable) {
        Lote lote = cargar(loteId);
        Comprador comprador = compradorRepository.obtenerComprador(compradorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comprador", compradorId));
        PuntoAlmacenamiento origen = almacenActualDe(lote);
        ReglaDeNegocioVioladaException.validar(origen.cubre(comprador.getMunicipio()), "R15",
                origen.nombre() + " no entrega en " + comprador.getMunicipio() + ".");
        despachar(lote, "Comprador " + comprador.getNombre(), tiempoTransito, responsable);
    }

    @Override
    public void confirmarEntrega(LoteId lote, String receptor, String responsable) {
        ejecutarSobre(lote, agregado -> agregado.confirmarEntrega(receptor, responsable, reloj.ahora()));
    }

    private void despachar(Lote lote, String destino, Duration tiempoTransito, String responsable) {
        Instant ahora = reloj.ahora();
        PuntoAlmacenamiento origen = almacenActualDe(lote);
        List<Lote> candidatos = loteRepository.lotesEnAlmacen(origen.id(), lote.tipoCultivo());
        ReglaDeNegocioVioladaException.validar(SeleccionLotes.respetaPrioridadDeDespacho(lote, candidatos, ahora), "R16",
                "Hay otro lote del mismo cultivo que vence antes ("
                        + SeleccionLotes.ordenDeDespacho(candidatos, ahora).get(0).codigo() + ") y debe salir primero.");
        lote.despachar(destino, tiempoTransito, responsable, ahora);
        origen.liberarLote(lote.id());
        almacenRepository.registrar(origen);
        persistirYPublicar(lote);
    }

    private PuntoAlmacenamiento almacenActualDe(Lote lote) {
        AlmacenId actual = lote.almacenActual().orElseThrow(() -> new ReglaDeNegocioVioladaException("R9",
                "El lote " + lote.codigo() + " no esta en ningun almacenamiento."));
        return cargarAlmacen(actual);
    }

    private PuntoAlmacenamiento cargarAlmacen(AlmacenId id) {
        return almacenRepository.obtenerAlmacen(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Punto de almacenamiento", id));
    }
}
