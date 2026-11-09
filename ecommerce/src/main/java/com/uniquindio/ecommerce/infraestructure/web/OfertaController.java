package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.GestionarDescuentosUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CrearDescuentoRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.DescuentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ofertas")
@Tag(name = "Descuentos por temporada", description = "Campanas vigentes (publico) y su gestion (ADMIN)")
public class OfertaController {

    private final GestionarDescuentosUseCase descuentos;

    public OfertaController(GestionarDescuentosUseCase descuentos) {
        this.descuentos = descuentos;
    }

    @GetMapping("/vigentes")
    @Operation(summary = "Campanas vigentes hoy")
    public List<DescuentoResponse> vigentes() {
        return descuentos.vigentes().stream().map(DescuentoResponse::desde).toList();
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Todas las campanas (ADMIN)")
    public List<DescuentoResponse> todas() {
        return descuentos.todos().stream().map(DescuentoResponse::desde).toList();
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear campana (ADMIN)", description = "Reglas 21 y 22. Si varias aplican, se usa la mayor; no se acumulan.")
    public ResponseEntity<DescuentoResponse> crear(@Valid @RequestBody CrearDescuentoRequest r) {
        ProductoId producto = r.productoId() == null ? null : ProductoId.de(r.productoId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DescuentoResponse.desde(descuentos.crear(r.nombre(), producto, r.porcentaje(), r.inicio(), r.fin())));
    }

    @PostMapping("/{id}/desactivar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Terminar una campana (ADMIN)")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        descuentos.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
