package com.uniquindio.ecommerce.application.port.out;

import com.uniquindio.ecommerce.domain.event.EventoDominio;

import java.util.List;

/**
 * Puerto de salida para publicar los eventos que el agregado acumulo.
 *
 * <p>El caso de uso lo invoca <b>despues</b> de guardar: si la persistencia falla,
 * no se anuncia un hecho que no ocurrio. La implementacion puede ser el
 * {@code ApplicationEventPublisher} de Spring, una cola o un simple log.</p>
 */
public interface PublicadorEventos {

    void publicar(EventoDominio evento);

    default void publicarTodos(List<EventoDominio> eventos) {
        eventos.forEach(this::publicar);
    }
}
