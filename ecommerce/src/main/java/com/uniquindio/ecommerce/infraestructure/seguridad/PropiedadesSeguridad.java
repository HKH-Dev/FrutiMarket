package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuracion de seguridad leida de {@code frutimarket.seguridad.*} (application.properties / .env).
 *
 * @param jwtSecreto         clave para firmar los tokens (HS256); minimo 32 caracteres.
 * @param expiracionMinutos  vigencia de cada token.
 * @param usuarios           usuarios de demostracion mientras no exista registro de usuarios.
 */
@ConfigurationProperties(prefix = "frutimarket.seguridad")
public record PropiedadesSeguridad(String jwtSecreto, long expiracionMinutos, List<Usuario> usuarios) {

    public PropiedadesSeguridad {
        if (jwtSecreto == null || jwtSecreto.length() < 32) {
            throw new IllegalStateException("frutimarket.seguridad.jwt-secreto (JWT_SECRET) debe tener al menos 32 caracteres.");
        }
        usuarios = usuarios == null ? List.of() : List.copyOf(usuarios);
    }

    public record Usuario(String nombre, String clave, Rol rol) {
    }
}
