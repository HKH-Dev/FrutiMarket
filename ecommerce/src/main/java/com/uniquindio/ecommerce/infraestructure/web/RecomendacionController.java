package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.RecomendarProductosUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.SugerenciaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recomendaciones")
@Tag(name = "Recomendaciones", description = "Personalizadas con token de comprador; generales sin el")
public class RecomendacionController {

    private final RecomendarProductosUseCase recomendaciones;

    public RecomendacionController(RecomendarProductosUseCase recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    @GetMapping
    @Operation(summary = "Productos recomendados con su motivo")
    public List<SugerenciaResponse> recomendar(@RequestParam(defaultValue = "6") int limite,
                                               @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        boolean esComprador = token != null && token.getClaimAsStringList("roles").contains("COMPRADOR");
        CompradorId comprador = esComprador ? com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual.comprador(token) : null;
        return recomendaciones.recomendar(comprador, Math.clamp(limite, 1, 20)).stream().map(SugerenciaResponse::desde).toList();
    }
}
