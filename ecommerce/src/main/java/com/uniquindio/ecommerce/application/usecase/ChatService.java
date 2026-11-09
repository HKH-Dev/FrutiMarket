package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ChatUseCase;
import com.uniquindio.ecommerce.application.port.out.NotificadorChat;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.chat.Conversacion;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.repository.ConversacionRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Sin {@code @Transactional} a proposito: cada adaptador confirma al guardar, asi el mensaje
 * se entrega en vivo solo cuando ya quedo persistido.
 */
@Service
public class ChatService implements ChatUseCase {

    private final ConversacionRepository conversaciones;
    private final LoteRepository lotes;
    private final CompradorRepository compradores;
    private final CampesinoRepository campesinos;
    private final NotificadorChat notificador;
    private final Reloj reloj;

    public ChatService(ConversacionRepository conversaciones, LoteRepository lotes, CompradorRepository compradores,
                       CampesinoRepository campesinos, NotificadorChat notificador, Reloj reloj) {
        this.conversaciones = conversaciones;
        this.lotes = lotes;
        this.compradores = compradores;
        this.campesinos = campesinos;
        this.notificador = notificador;
        this.reloj = reloj;
    }

    @Override
    public VistaConversacion iniciar(CompradorId comprador, LoteId lote) {
        CampesinoId campesino = lotes.obtenerLote(lote).orElseThrow(() -> new RecursoNoEncontradoException("Lote", lote))
                .campesinoResponsable();
        Conversacion conversacion = conversaciones.entre(comprador, campesino).orElseGet(() -> {
            Conversacion nueva = Conversacion.iniciar(comprador, campesino, reloj.ahora());
            conversaciones.guardar(nueva);
            return nueva;
        });
        return aVista(conversacion);
    }

    @Override
    public List<VistaConversacion> delActor(UUID actor) {
        return conversaciones.delActor(actor).stream().map(this::aVista)
                .sorted(Comparator.comparing(VistaConversacion::ultimaActividad).reversed()).toList();
    }

    @Override
    public List<Mensaje> mensajes(UUID conversacion, UUID actor) {
        cargar(conversacion).exigirParticipante(actor);
        return conversaciones.mensajes(conversacion);
    }

    @Override
    public Mensaje enviar(UUID conversacion, UUID autor, String texto) {
        Instant ahora = reloj.ahora();
        Mensaje mensaje = cargar(conversacion).escribir(autor, texto, ahora);
        conversaciones.guardarMensaje(mensaje);
        notificador.entregar(mensaje);
        return mensaje;
    }

    @Override
    public boolean participa(UUID conversacion, UUID actor) {
        return conversaciones.obtener(conversacion).map(c -> c.participa(actor)).orElse(false);
    }

    private Conversacion cargar(UUID id) {
        return conversaciones.obtener(id).orElseThrow(() -> new RecursoNoEncontradoException("Conversacion", id));
    }

    private VistaConversacion aVista(Conversacion c) {
        List<Mensaje> mensajes = conversaciones.mensajes(c.id());
        Mensaje ultimo = mensajes.isEmpty() ? null : mensajes.get(mensajes.size() - 1);
        String comprador = compradores.obtenerComprador(c.comprador()).map(Comprador::getNombre).orElse("Comprador");
        String campesino = campesinos.obtenerCampesino(c.campesino()).map(Campesino::getNombre).orElse("Campesino");
        return new VistaConversacion(c.id(), c.comprador().valor(), comprador, c.campesino().valor(), campesino,
                ultimo == null ? null : ultimo.texto(), ultimo == null ? c.creada() : ultimo.momento());
    }
}
