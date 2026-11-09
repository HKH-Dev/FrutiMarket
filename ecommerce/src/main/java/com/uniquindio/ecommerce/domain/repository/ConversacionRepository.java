package com.uniquindio.ecommerce.domain.repository;

import com.uniquindio.ecommerce.domain.chat.Conversacion;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversacionRepository {

    void guardar(Conversacion conversacion);

    Optional<Conversacion> obtener(UUID id);

    Optional<Conversacion> entre(CompradorId comprador, CampesinoId campesino);

    /** Conversaciones donde participa el actor, sea comprador o campesino. */
    List<Conversacion> delActor(UUID actor);

    void guardarMensaje(Mensaje mensaje);

    /** En orden cronologico. */
    List<Mensaje> mensajes(UUID conversacion);
}
