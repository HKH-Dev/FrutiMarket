package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.infraestructure.seguridad.EmisorTokens;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Obtener un token JWT para las rutas protegidas")
public class AuthController {

    private final AuthenticationManager autenticador;
    private final EmisorTokens emisor;

    public AuthController(AuthenticationManager autenticador, EmisorTokens emisor) {
        this.autenticador = autenticador;
        this.emisor = emisor;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT. Copielo en el boton Authorize de Swagger.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var autenticado = autenticador.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.clave()));
        EmisorTokens.TokenEmitido token = emisor.emitir(autenticado);
        return new TokenResponse(token.token(), "Bearer", token.expiraEn(), token.roles());
    }

    public record LoginRequest(@NotBlank @Schema(example = "admin") String usuario,
                               @NotBlank @Schema(example = "admin123") String clave) {
    }

    public record TokenResponse(String token, String tipo, Instant expiraEn, List<String> roles) {
    }
}
