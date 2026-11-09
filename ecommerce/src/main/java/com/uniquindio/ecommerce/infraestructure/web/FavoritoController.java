package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.GestionarFavoritosUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Solo COMPRADOR: lista de deseos por producto")
@SecurityRequirement(name = "bearerAuth")
public class FavoritoController {

    private final GestionarFavoritosUseCase favoritos;

    public FavoritoController(GestionarFavoritosUseCase favoritos) {
        this.favoritos = favoritos;
    }

    @GetMapping
    @Operation(summary = "Ids de mis productos favoritos")
    public List<UUID> listar(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return favoritos.favoritosDe(UsuarioActual.comprador(token)).stream().map(ProductoId::valor).toList();
    }

    @PutMapping("/{productoId}")
    @Operation(summary = "Agregar a favoritos", description = "Idempotente.")
    public ResponseEntity<Void> agregar(@PathVariable UUID productoId, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        favoritos.agregar(UsuarioActual.comprador(token), ProductoId.de(productoId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productoId}")
    @Operation(summary = "Quitar de favoritos")
    public ResponseEntity<Void> quitar(@PathVariable UUID productoId, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        favoritos.quitar(UsuarioActual.comprador(token), ProductoId.de(productoId));
        return ResponseEntity.noContent().build();
    }
}
