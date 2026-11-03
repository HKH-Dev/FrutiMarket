package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.infraestructure.seguridad.EmisorTokens;
import com.uniquindio.ecommerce.infraestructure.seguridad.Rol;
import com.uniquindio.ecommerce.infraestructure.seguridad.ServicioUsuarios;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion (token JWT)")
public class AuthController {

    private final AuthenticationManager autenticador;
    private final EmisorTokens emisor;
    private final ServicioUsuarios usuarios;

    public AuthController(AuthenticationManager autenticador, EmisorTokens emisor, ServicioUsuarios usuarios) {
        this.autenticador = autenticador;
        this.emisor = emisor;
        this.usuarios = usuarios;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar un usuario",
            description = "Crea un usuario COMPRADOR o CAMPESINO. La clave se guarda cifrada con BCrypt. "
                    + "Errores: 400 datos invalidos, 422 correo repetido (INV-CORREO) o rol no permitido (INV-ROL).")
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        ServicioUsuarios.UsuarioRegistrado creado = usuarios.registrar(request.correo(), request.clave(), request.rol());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistroResponse(creado.id(), creado.correo(), creado.rol()));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion",
            description = "Devuelve un token JWT con correo, roles y vencimiento. Copielo en el boton Authorize de Swagger. "
                    + "Errores: 400 datos invalidos, 401 correo o clave incorrectos.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var autenticado = autenticador.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.correo().trim().toLowerCase(), request.clave()));
        EmisorTokens.TokenEmitido token = emisor.emitir(autenticado);
        return new TokenResponse(token.token(), "Bearer", token.expiraEn(), token.roles());
    }

    public record LoginRequest(@NotBlank @Schema(example = "admin@frutimarket.com") String correo,
                               @NotBlank @Schema(example = "admin123") String clave) {
    }

    public record RegistroRequest(@NotBlank @Email @Schema(example = "ana@correo.com") String correo,
                                  @NotBlank @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
                                  @Schema(example = "miClave123") String clave,
                                  @NotNull @Schema(description = "COMPRADOR o CAMPESINO", example = "COMPRADOR") Rol rol) {
    }

    public record RegistroResponse(UUID id, String correo, Rol rol) {
    }

    public record TokenResponse(String token, String tipo, Instant expiraEn, List<String> roles) {
    }
}