package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ChatUseCase {

    /** Abre (o retoma) la conversacion del comprador con el campesino dueno del lote. */
    VistaConversacion iniciar(CompradorId comprador, LoteId lote);

    List<VistaConversacion> delActor(UUID actor);

    List<Mensaje> mensajes(UUID conversacion, UUID actor);

    Mensaje enviar(UUID conversacion, UUID autor, String texto);

    boolean participa(UUID conversacion, UUID actor);

    record VistaConversacion(UUID id, UUID compradorId, String comprador, UUID campesinoId, String campesino,
                             String ultimoMensaje, Instant ultimaActividad) {
    }
}
