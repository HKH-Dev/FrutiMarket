package com.uniquindio.ecommerce.infraestructure.seguridad;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Firma los tokens JWT que entrega el login. */
@Component
public class EmisorTokens {

    static final String CLAIM_CORREO = "correo";
    /** Id del campesino o comprador que representa el usuario. */
    static final String CLAIM_ACTOR = "actorId";

    private final JwtEncoder encoder;
    private final Duration vigencia;

    public EmisorTokens(JwtEncoder encoder, PropiedadesSeguridad propiedades) {
        this.encoder = encoder;
        this.vigencia = Duration.ofMinutes(propiedades.expiracionMinutos());
    }

    /**
     * Firma un token para el usuario. Claims: {@code sub} y {@code correo} (el correo), {@code roles},
     * {@code actorId} (su CampesinoId o CompradorId, si tiene) y la fecha de vencimiento {@code exp}.
     */
    public TokenEmitido emitir(String correo, List<String> roles, UUID actorId) {
        Instant ahora = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("frutimarket")
                .subject(correo)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(vigencia))
                .claim(CLAIM_CORREO, correo)
                .claim(SeguridadConfig.CLAIM_ROLES, roles);
        if (actorId != null) {
            claims.claim(CLAIM_ACTOR, actorId.toString());
        }
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims.build()))
                .getTokenValue();
        return new TokenEmitido(token, correo, ahora.plus(vigencia), roles);
    }

    public record TokenEmitido(String token, String correo, Instant expiraEn, List<String> roles) {
    }
}
