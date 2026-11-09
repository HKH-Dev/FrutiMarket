package com.uniquindio.ecommerce.infraestructure.chat;

import com.uniquindio.ecommerce.application.port.in.ChatUseCase;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.util.UUID;

/** Mensajes que llegan por WebSocket: {@code SEND /app/conversaciones/{id}}. */
@Controller
public class ChatStompController {

    private static final Logger LOG = LoggerFactory.getLogger(ChatStompController.class);

    private final ChatUseCase chat;

    public ChatStompController(ChatUseCase chat) {
        this.chat = chat;
    }

    public record MensajeEntrante(String texto) {
    }

    @MessageMapping("/conversaciones/{id}")
    public void enviar(@DestinationVariable UUID id, @Payload MensajeEntrante mensaje, JwtAuthenticationToken usuario) {
        chat.enviar(id, UsuarioActual.actor(usuario.getToken()), mensaje.texto());
    }

    @MessageExceptionHandler(ReglaDeNegocioVioladaException.class)
    public void reglaViolada(ReglaDeNegocioVioladaException ex) {
        LOG.info("Mensaje rechazado: {}", ex.getMessage());
    }
}
