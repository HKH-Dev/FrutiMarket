package com.uniquindio.ecommerce.application;

import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.exception.LoteNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base comun de los casos de uso que operan sobre un lote.
 *
 * <p>Todo caso de uso del agregado sigue el mismo guion: <i>cargar, ejecutar,
 * guardar, publicar</i>. Recogerlo aqui evita repetirlo seis veces y, sobre todo,
 * evita que alguien lo escriba mal: los eventos se publican <b>despues</b> de
 * guardar, nunca antes, para no anunciar un hecho que la base de datos rechazo.</p>
 *
 * <p>La clase no conoce Spring. Cuando se integre, basta anotar las subclases con
 * {@code @Service} y {@code @Transactional}; la logica no cambia.</p>
 */
abstract class ServicioDeAplicacion {

    protected final LoteRepository loteRepository;
    protected final Reloj reloj;
    protected final PublicadorEventos publicadorEventos;

    protected ServicioDeAplicacion(LoteRepository loteRepository, Reloj reloj, PublicadorEventos publicadorEventos) {
        this.loteRepository = Objects.requireNonNull(loteRepository, "El repositorio de lotes es obligatorio.");
        this.reloj = Objects.requireNonNull(reloj, "El reloj es obligatorio.");
        this.publicadorEventos = Objects.requireNonNull(publicadorEventos, "El publicador de eventos es obligatorio.");
    }

    /** Carga el lote o falla con una excepcion del dominio, nunca con {@code null}. */
    protected Lote cargar(LoteId id) {
        Objects.requireNonNull(id, "El identificador del lote es obligatorio.");
        return loteRepository.buscarPorId(id).orElseThrow(() -> new LoteNoEncontradoException(id));
    }

    /** Aplica una operacion al lote, lo guarda y publica los eventos que genero. */
    protected Lote ejecutarSobre(LoteId id, Consumer<Lote> operacion) {
        Lote lote = cargar(id);
        operacion.accept(lote);
        return persistirYPublicar(lote);
    }

    protected Lote persistirYPublicar(Lote lote) {
        Lote guardado = loteRepository.guardar(lote);
        publicadorEventos.publicarTodos(guardado.eventos());
        guardado.limpiarEventos();
        return guardado;
    }
}
