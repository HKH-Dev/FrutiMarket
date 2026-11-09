package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.GestionarCuponesUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CrearCuponRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CuponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cupones")
@Tag(name = "Cupones", description = "Cupones personalizados por comprador")
@SecurityRequirement(name = "bearerAuth")
public class CuponController {

    private final GestionarCuponesUseCase cupones;

    public CuponController(GestionarCuponesUseCase cupones) {
        this.cupones = cupones;
    }

    @GetMapping("/mios")
    @Operation(summary = "Mis cupones (COMPRADOR)")
    public List<CuponResponse> mios(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return cupones.delComprador(UsuarioActual.comprador(token)).stream().map(CuponResponse::desde).toList();
    }

    @GetMapping
    @Operation(summary = "Todos los cupones (ADMIN)")
    public List<CuponResponse> todos() {
        return cupones.todos().stream().map(CuponResponse::desde).toList();
    }

    @PostMapping
    @Operation(summary = "Crear cupon para un comprador (ADMIN)", description = "Un solo uso (regla 24) y solo lo redime su dueno (regla 23).")
    public ResponseEntity<CuponResponse> crear(@Valid @RequestBody CrearCuponRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CuponResponse.desde(cupones.crear(r.codigo(), CompradorId.de(r.compradorId()), r.porcentaje(), r.venceEl())));
    }
}
