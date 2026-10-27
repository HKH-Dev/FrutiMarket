package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.RealizarCompraUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compras")
@Tag(name = "Compras", description = "Compra de uno o varios lotes")
public class CompraController {

    private final RealizarCompraUseCase realizarCompra;

    public CompraController(RealizarCompraUseCase realizarCompra) {
        this.realizarCompra = realizarCompra;
    }

    @PostMapping
    @Operation(summary = "Realizar una compra",
            description = "Reserva cada item (regla 3) y confirma la compra. Si un item falla, no se compra nada. "
                    + "El precio se toma del lote, no del cliente.")
    public ResponseEntity<CompraResponse> comprar(@Valid @RequestBody RealizarCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(realizarCompra.comprar(request));
    }
}
