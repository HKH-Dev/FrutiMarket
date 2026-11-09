package com.uniquindio.ecommerce.domain.chat;

import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.time.Instant;
import java.util.UUID;

/**
 * Conversacion entre un comprador y un campesino (una por pareja).
 * <b>R26:</b> solo sus dos participantes leen o escriben. <b>R27:</b> el mensaje no va vacio y tiene maximo 1000 caracteres.
 */
public final class Conversacion {

    public static final int LARGO_MAXIMO = 1000;

    private final UUID id;
    private final CompradorId comprador;
    private final CampesinoId campesino;
    private final Instant creada;

    private Conversacion(UUID id, CompradorId comprador, CampesinoId campesino, Instant creada) {
        this.id = id;
        this.comprador = comprador;
        this.campesino = campesino;
        this.creada = creada;
    }

    public static Conversacion iniciar(CompradorId comprador, CampesinoId campesino, Instant momento) {
        return new Conversacion(UUID.randomUUID(), comprador, campesino, momento);
    }

    public static Conversacion reconstituir(UUID id, CompradorId comprador, CampesinoId campesino, Instant creada) {
        return new Conversacion(id, comprador, campesino, creada);
    }

    public boolean participa(UUID actor) {
        return comprador.valor().equals(actor) || campesino.valor().equals(actor);
    }

    public void exigirParticipante(UUID actor) {
        ReglaDeNegocioVioladaException.validar(participa(actor), "R26", "No participa en esta conversacion.");
    }

    public Mensaje escribir(UUID autor, String texto, Instant momento) {
        exigirParticipante(autor);
        ReglaDeNegocioVioladaException.validar(texto != null && !texto.isBlank(), "R27", "El mensaje no puede estar vacio.");
        ReglaDeNegocioVioladaException.validar(texto.length() <= LARGO_MAXIMO, "R27",
                "El mensaje no puede superar " + LARGO_MAXIMO + " caracteres.");
        return new Mensaje(UUID.randomUUID(), id, autor, texto.trim(), momento);
    }

    public UUID id() { return id; }
    public CompradorId comprador() { return comprador; }
    public CampesinoId campesino() { return campesino; }
    public Instant creada() { return creada; }
}
