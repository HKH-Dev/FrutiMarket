package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.GestionarDisponibilidadUseCase;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.MermaRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.ReservaRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.CantidadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Cantidad disponible, reservas de pedidos y merma (regla 3). */
@RestController
@RequestMapping("/api/lotes/{id}")
@Tag(name = "Disponibilidad", description = "Reservas, salidas y merma de un lote")
public class DisponibilidadController {

    private final GestionarDisponibilidadUseCase disponibilidad;

    public DisponibilidadController(GestionarDisponibilidadUseCase disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    @GetMapping("/disponible")
    @Operation(summary = "Cantidad disponible del lote")
    public CantidadResponse disponible(@PathVariable UUID id) {
        return CantidadResponse.desde(disponibilidad.consultarDisponible(LoteId.de(id)));
    }

    @PostMapping("/reservas")
    @Operation(summary = "Reservar cantidad para un pedido", description = "Regla 3: nunca mas de lo disponible.")
    public ResponseEntity<Void> reservar(@PathVariable UUID id, @Valid @RequestBody ReservaRequest request) {
        LoteId lote = LoteId.de(id);
        // La cantidad se expresa en la unidad del lote: el cliente solo envia el numero.
        Cantidad cantidad = Cantidad.de(request.cantidad(), disponibilidad.consultarDisponible(lote).unidad());
        disponibilidad.reservar(lote, PedidoId.de(request.pedidoId()), cantidad);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/reservas/{pedidoId}")
    @Operation(summary = "Liberar la reserva de un pedido", description = "La cantidad vuelve a estar disponible.")
    public ResponseEntity<Void> liberar(@PathVariable UUID id, @PathVariable UUID pedidoId) {
        disponibilidad.liberar(LoteId.de(id), PedidoId.de(pedidoId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservas/{pedidoId}/salida")
    @Operation(summary = "Confirmar la salida de un pedido reservado")
    public ResponseEntity<Void> confirmarSalida(@PathVariable UUID id, @PathVariable UUID pedidoId) {
        disponibilidad.confirmarSalida(LoteId.de(id), PedidoId.de(pedidoId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merma")
    @Operation(summary = "Redefinir la merma esperada", description = "Solo mientras el lote esta en borrador (REGISTRADO).")
    public ResponseEntity<Void> registrarMerma(@PathVariable UUID id, @Valid @RequestBody MermaRequest request) {
        disponibilidad.registrarMerma(LoteId.de(id), CampesinoId.de(request.campesinoId()),
                Merma.de(request.porcentaje(), request.justificacion()));
        return ResponseEntity.noContent().build();
    }
}
