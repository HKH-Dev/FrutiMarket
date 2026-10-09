package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.function.Consumer;

/**
 * Base comun de los casos de uso sobre un lote: <i>cargar, ejecutar, guardar, publicar</i>.
 * Los eventos se publican despues de guardar, nunca antes. No conoce Spring: al integrarlo
 * basta anotar las subclases con {@code @Service} y {@code @Transactional}.
 */
abstract class ServicioDeAplicacion {

    protected final LoteRepository loteRepository;
    protected final Reloj reloj;
    protected final PublicadorEventos publicadorEventos;

    protected ServicioDeAplicacion(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        this.loteRepository = loteRepository;
        this.reloj = reloj;
        this.publicadorEventos = publicadorEventos;
    }

    protected Lote cargar(LoteId id) {
        return loteRepository.obtenerLote(id).orElseThrow(() -> new RecursoNoEncontradoException("Lote", id));
    }

    /** Regla 5: solo el campesino responsable modifica su lote. */
    protected Lote cargarComoResponsable(LoteId id, CampesinoId solicitante) {
        Lote lote = cargar(id);
        ReglaDeNegocioVioladaException.validar(lote.esDelCampesino(solicitante), "R5",
                "El campesino " + solicitante + " no es responsable del lote " + lote.codigo() + ".");
        return lote;
    }

    protected void ejecutarSobre(LoteId id, Consumer<Lote> operacion) {
        Lote lote = cargar(id);
        operacion.accept(lote);
        persistirYPublicar(lote);
    }

    protected Lote persistirYPublicar(Lote lote) {
        Lote guardado = loteRepository.almacenar(lote);
        publicadorEventos.publicarTodos(guardado.eventos());
        guardado.limpiarEventos();
        return guardado;
    }
}
