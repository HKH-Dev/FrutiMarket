package com.uniquindio.ecommerce.application.port.out;

import com.uniquindio.ecommerce.domain.chat.Mensaje;

/** Entrega en vivo un mensaje ya guardado a quienes tienen abierta la conversacion. */
public interface NotificadorChat {

    void entregar(Mensaje mensaje);
}
