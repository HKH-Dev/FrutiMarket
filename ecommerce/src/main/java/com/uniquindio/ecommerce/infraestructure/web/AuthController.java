package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.domain.entity.Comprador;
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
            description = "Crea un usuario COMPRADOR o CAMPESINO y su perfil (comprador o campesino). La clave se guarda "
                    + "cifrada con BCrypt. Un campesino nuevo no puede ofrecer lotes hasta que un ADMIN lo autorice (regla 1). "
                    + "Errores: 400 datos invalidos, 422 correo repetido (INV-CORREO), rol no permitido (INV-ROL) "
                    + "o falta un dato del perfil (INV-REGISTRO).")
    public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        ServicioUsuarios.UsuarioRegistrado creado = usuarios.registrar(new ServicioUsuarios.DatosRegistro(
                request.correo(), request.clave(), request.rol(), request.nombre(), request.numeroIdentificacion(),
                request.municipio(), request.tipoComprador()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistroResponse(creado.id(), creado.correo(), creado.rol(), creado.actorId()));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion",
            description = "Devuelve un token JWT con el correo, el rol y la fecha de vencimiento. Copielo en el boton "
                    + "Authorize de Swagger. Errores: 400 datos invalidos, 401 correo o clave incorrectos.")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var autenticado = autenticador.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.correo().trim().toLowerCase(), request.clave()));
        ServicioUsuarios.UsuarioRegistrado usuario = usuarios.buscarPorCorreo(autenticado.getName()).orElseThrow();
        EmisorTokens.TokenEmitido token = emisor.emitir(usuario.correo(), List.of(usuario.rol().name()), usuario.actorId());
        return new TokenResponse(token.token(), "Bearer", token.correo(), usuario.rol().name(), token.expiraEn());
    }

    public record LoginRequest(@NotBlank @Schema(example = "campesino@frutimarket.com") String correo,
                               @NotBlank @Schema(example = "campesino123") String clave) {
    }

    public record RegistroRequest(@NotBlank @Email @Schema(example = "ana@correo.com") String correo,
                                  @NotBlank @Size(min = 8, max = 72, message = "La clave debe tener entre 8 y 72 caracteres")
                                  @Schema(example = "miClave123") String clave,
                                  @NotNull @Schema(description = "COMPRADOR o CAMPESINO", example = "COMPRADOR") Rol rol,
                                  @NotBlank @Size(max = 120) @Schema(example = "Ana Lopez") String nombre,
                                  @Size(max = 30) @Schema(description = "Obligatorio para CAMPESINO", example = "1094123456")
                                  String numeroIdentificacion,
                                  @Size(max = 80) @Schema(description = "Obligatorio para COMPRADOR", example = "Armenia")
                                  String municipio,
                                  @Schema(description = "Solo COMPRADOR; por defecto CONSUMIDOR", example = "RESTAURANTE")
                                  Comprador.Tipo tipoComprador) {
    }

    /** {@code perfilId}: id del campesino o comprador creado con el usuario. */
    public record RegistroResponse(UUID id, String correo, Rol rol, UUID perfilId) {
    }

    public record TokenResponse(String token, String tipo, String correo, String rol, Instant expiraEn) {
    }
}
