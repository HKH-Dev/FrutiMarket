package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.ConsultarComprasUseCase;
import com.uniquindio.ecommerce.application.port.in.RealizarCompraUseCase;
import com.uniquindio.ecommerce.application.port.in.SolicitarReembolsoUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Compras del comprador del token: comprar, consultar y pedir reembolso. */
@RestController
@RequestMapping("/api/compras")
@Tag(name = "Compras", description = "Solo COMPRADOR: comprar, mis compras y reembolsos")
@SecurityRequirement(name = "bearerAuth")
public class CompraController {

    private final RealizarCompraUseCase realizarCompra;
    private final ConsultarComprasUseCase consultarCompras;
    private final SolicitarReembolsoUseCase solicitarReembolso;

    public CompraController(RealizarCompraUseCase realizarCompra, ConsultarComprasUseCase consultarCompras,
                            SolicitarReembolsoUseCase solicitarReembolso) {
        this.realizarCompra = realizarCompra;
        this.consultarCompras = consultarCompras;
        this.solicitarReembolso = solicitarReembolso;
    }

    @PostMapping
    @Operation(summary = "Realizar una compra",
            description = "Reserva cada item (regla 3) y confirma la compra. Si un item falla, no se compra nada. "
                    + "El precio se toma del lote, no del cliente, y el comprador es el del token.")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody RealizarCompraRequest request,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(realizarCompra.comprar(UsuarioActual.comprador(token), request));
    }

    @GetMapping("/mias")
    @Operation(summary = "Mis compras", description = "Las compras del comprador del token, de la mas reciente a la mas antigua.")
    public List<CompraResponse> misCompras(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return consultarCompras.comprasDe(UsuarioActual.comprador(token));
    }

    @PostMapping("/{id}/reembolso")
    @Operation(summary = "Pedir el reembolso de una compra",
            description = "Regla 19: solo el comprador que la hizo. Regla 20: dentro de los 3 dias siguientes a la compra. "
                    + "Solo compras COMPLETADAS (INV-COMPRA-REEMBOLSO). Errores: 404 no existe, 422 regla rota.")
    public CompraResponse reembolsar(@PathVariable UUID id,
                                     @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return solicitarReembolso.reembolsar(PedidoId.de(id), UsuarioActual.comprador(token));
    }
}
