package com.uniquindio.ecommerce.infraestructure.seguridad;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Lee del token quien hace la peticion. Los controllers lo usan en lugar de recibir
 * {@code campesinoId} o {@code compradorId} en el cuerpo: asi nadie puede actuar en nombre de otro.
 */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static CampesinoId campesino(Jwt token) {
        return CampesinoId.de(actor(token, "un campesino"));
    }

    public static CompradorId comprador(Jwt token) {
        return CompradorId.de(actor(token, "un comprador"));
    }

    private static String actor(Jwt token, String queSeEsperaba) {
        String actor = token == null ? null : token.getClaimAsString(EmisorTokens.CLAIM_ACTOR);
        if (actor == null) {
            throw new AccessDeniedException("Su usuario no esta vinculado a " + queSeEsperaba + ".");
        }
        return actor;
    }
}
