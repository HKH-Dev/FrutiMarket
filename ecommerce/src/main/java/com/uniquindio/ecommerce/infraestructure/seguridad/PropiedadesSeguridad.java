package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.UUID;

/**
 * Configuracion de seguridad leida de {@code frutimarket.seguridad.*} (application.properties / .env).
 *
 * @param jwtSecreto         clave para firmar los tokens (HS256); minimo 32 caracteres.
 * @param expiracionMinutos  vigencia de cada token.
 * @param usuarios           usuarios de demostracion que se crean al arrancar.
 */
@ConfigurationProperties(prefix = "frutimarket.seguridad")
public record PropiedadesSeguridad(String jwtSecreto, long expiracionMinutos, List<Usuario> usuarios) {

    public PropiedadesSeguridad {
        if (jwtSecreto == null || jwtSecreto.length() < 32) {
            throw new IllegalStateException("frutimarket.seguridad.jwt-secreto (JWT_SECRET) debe tener al menos 32 caracteres.");
        }
        usuarios = usuarios == null ? List.of() : List.copyOf(usuarios);
    }

    /** {@code actorId}: campesino o comprador de los datos de ejemplo al que queda vinculado (nulo para ADMIN). */
    public record Usuario(String correo, String clave, Rol rol, UUID actorId) {
    }
}
