package com.uniquindio.ecommerce.infraestructure;

import com.uniquindio.ecommerce.application.port.in.ChatUseCase;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chat en vivo con STOMP sobre WebSocket.
 *
 * <p>El navegador no puede poner cabeceras en el handshake, y un token en la URL quedaria en los logs.
 * Por eso {@code /ws} es abierto y la autenticacion ocurre en el frame CONNECT, con el mismo JWT de la
 * API. Cada SUBSCRIBE se autoriza: solo los participantes escuchan una conversacion (regla 26).</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Pattern TOPICO_CONVERSACION = Pattern.compile("^/topic/conversaciones/([0-9a-fA-F-]{36})$");

    private final JwtDecoder decodificador;
    private final ChatUseCase chat;
    private final String[] origenes;

    public WebSocketConfig(JwtDecoder decodificador, @Lazy ChatUseCase chat,
                           @Value("${frutimarket.cors.origenes:http://localhost:4200}") String[] origenes) {
        this.decodificador = decodificador;
        this.chat = chat;
        this.origenes = origenes;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registro) {
        registro.enableSimpleBroker("/topic");
        registro.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registro) {
        registro.addEndpoint("/ws").setAllowedOrigins(origenes);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration canal) {
        canal.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> mensaje, MessageChannel destino) {
                StompHeaderAccessor cabeceras = MessageHeaderAccessor.getAccessor(mensaje, StompHeaderAccessor.class);
                if (cabeceras == null || cabeceras.getCommand() == null) {
                    return mensaje;
                }
                if (cabeceras.getCommand() == StompCommand.CONNECT) {
                    cabeceras.setUser(autenticar(cabeceras.getFirstNativeHeader("Authorization"), mensaje));
                } else if (cabeceras.getCommand() == StompCommand.SUBSCRIBE) {
                    autorizarSuscripcion(cabeceras, mensaje);
                }
                return mensaje;
            }
        });
    }

    private JwtAuthenticationToken autenticar(String autorizacion, Message<?> mensaje) {
        if (autorizacion == null || !autorizacion.startsWith("Bearer ")) {
            throw new MessageDeliveryException(mensaje, "Falta el token");
        }
        try {
            Jwt token = decodificador.decode(autorizacion.substring("Bearer ".length()));
            List<String> roles = token.getClaimAsStringList("roles");
            return new JwtAuthenticationToken(token, roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());
        } catch (JwtException ex) {
            throw new MessageDeliveryException(mensaje, "Token invalido");
        }
    }

    private void autorizarSuscripcion(StompHeaderAccessor cabeceras, Message<?> mensaje) {
        Matcher topico = TOPICO_CONVERSACION.matcher(String.valueOf(cabeceras.getDestination()));
        boolean permitido = topico.matches() && cabeceras.getUser() instanceof JwtAuthenticationToken usuario
                && chat.participa(UUID.fromString(topico.group(1)), UsuarioActual.actor(usuario.getToken()));
        if (!permitido) {
            throw new MessageDeliveryException(mensaje, "No puede escuchar esta conversacion");
        }
    }
}
