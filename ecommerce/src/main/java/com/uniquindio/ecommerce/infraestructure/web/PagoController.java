package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.PagarCompraUseCase;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CheckoutRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.ComprobanteResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.CotizacionRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.ComercialDtos.LiquidacionResponse;
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

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Solo COMPRADOR: cotizacion y pago simulado con confirmacion automatica")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagarCompraUseCase pagos;

    public PagoController(PagarCompraUseCase pagos) {
        this.pagos = pagos;
    }

    @PostMapping("/cotizacion")
    @Operation(summary = "Calcular el total con descuentos", description = "No guarda nada ni consume el cupon.")
    public LiquidacionResponse cotizar(@Valid @RequestBody CotizacionRequest r,
                                       @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return LiquidacionResponse.desde(pagos.cotizar(UsuarioActual.comprador(token), r.items(), r.cupon()));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Comprar y pagar", description = "Pasarela simulada. Tarjeta terminada en 0000: rechazo (422, regla R25) y no se compra nada.")
    public ResponseEntity<ComprobanteResponse> pagar(@Valid @RequestBody CheckoutRequest r,
                                                     @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ComprobanteResponse.desde(
                pagos.pagar(UsuarioActual.comprador(token), new RealizarCompraRequest(r.items()), r.cupon(), r.metodo(), r.ultimosDigitos())));
    }
}
