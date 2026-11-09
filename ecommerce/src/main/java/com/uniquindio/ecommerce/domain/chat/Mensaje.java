package com.uniquindio.ecommerce.domain.chat;

import java.time.Instant;
import java.util.UUID;

/** Value Object: mensaje ya validado por {@link Conversacion#escribir}. */
public record Mensaje(UUID id, UUID conversacion, UUID autor, String texto, Instant momento) {
}
