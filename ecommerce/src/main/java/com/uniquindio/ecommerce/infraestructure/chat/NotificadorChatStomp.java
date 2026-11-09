package com.uniquindio.ecommerce.infraestructure.chat;

import com.uniquindio.ecommerce.application.port.out.NotificadorChat;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.MensajeResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Publica cada mensaje en el topico de su conversacion. */
@Component
public class NotificadorChatStomp implements NotificadorChat {

    private final SimpMessagingTemplate mensajeria;

    public NotificadorChatStomp(SimpMessagingTemplate mensajeria) {
        this.mensajeria = mensajeria;
    }

    @Override
    public void entregar(Mensaje mensaje) {
        mensajeria.convertAndSend("/topic/conversaciones/" + mensaje.conversacion(), MensajeResponse.desde(mensaje));
    }
}
