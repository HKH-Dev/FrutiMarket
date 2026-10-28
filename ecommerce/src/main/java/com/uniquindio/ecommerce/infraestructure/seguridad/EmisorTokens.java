package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Firma los tokens JWT que entrega el login. */
@Component
public class EmisorTokens {

    private final JwtEncoder encoder;
    private final Duration vigencia;

    public EmisorTokens(JwtEncoder encoder, PropiedadesSeguridad propiedades) {
        this.encoder = encoder;
        this.vigencia = Duration.ofMinutes(propiedades.expiracionMinutos());
    }

    public TokenEmitido emitir(Authentication autenticado) {
        List<String> roles = autenticado.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autoridad -> autoridad.replaceFirst("^ROLE_", ""))
                .toList();
        return emitir(autenticado.getName(), roles);
    }

    /** Tambien lo usan las pruebas para obtener tokens con un rol concreto. */
    public TokenEmitido emitir(String usuario, List<String> roles) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("frutimarket")
                .subject(usuario)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(vigencia))
                .claim(SeguridadConfig.CLAIM_ROLES, roles)
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenEmitido(token, ahora.plus(vigencia), roles);
    }

    public record TokenEmitido(String token, Instant expiraEn, List<String> roles) {
    }
}
