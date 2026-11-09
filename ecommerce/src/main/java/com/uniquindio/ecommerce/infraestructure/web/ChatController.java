package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ChatUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.ConversacionResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.EnviarMensajeRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.IniciarConversacionRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.MensajeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Historial y envio por HTTP. En vivo: STOMP en /ws, topico /topic/conversaciones/{id}. */
@RestController
@RequestMapping("/api/chat/conversaciones")
@Tag(name = "Chat", description = "Comprador y campesino. Solo los participantes leen o escriben (R26)")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatUseCase chat;

    public ChatController(ChatUseCase chat) {
        this.chat = chat;
    }

    @PostMapping
    @Operation(summary = "Abrir o retomar la conversacion con el vendedor de un lote (COMPRADOR)")
    public ResponseEntity<ConversacionResponse> iniciar(@Valid @RequestBody IniciarConversacionRequest r,
                                                        @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ConversacionResponse.desde(chat.iniciar(UsuarioActual.comprador(token), LoteId.de(r.loteId()))));
    }

    @GetMapping
    @Operation(summary = "Mis conversaciones")
    public List<ConversacionResponse> mias(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return chat.delActor(UsuarioActual.actor(token)).stream().map(ConversacionResponse::desde).toList();
    }

    @GetMapping("/{id}/mensajes")
    @Operation(summary = "Historial de una conversacion")
    public List<MensajeResponse> mensajes(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return chat.mensajes(id, UsuarioActual.actor(token)).stream().map(MensajeResponse::desde).toList();
    }

    @PostMapping("/{id}/mensajes")
    @Operation(summary = "Enviar un mensaje", description = "Tambien se entrega en vivo a quien tenga abierta la conversacion.")
    public ResponseEntity<MensajeResponse> enviar(@PathVariable UUID id, @Valid @RequestBody EnviarMensajeRequest r,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MensajeResponse.desde(chat.enviar(id, UsuarioActual.actor(token), r.texto())));
    }
}
