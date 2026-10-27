package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.GestionarLogisticaLoteUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.DespachoAlmacenRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.DespachoCompradorRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.EntregaRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.IngresoAlmacenRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.LoteResumenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** Movimiento fisico del lote: finca, acopio, redistribucion y comprador. */
@RestController
@Tag(name = "Logistica", description = "Cadena de custodia: almacenamiento, despachos y entrega")
public class LogisticaController {

    private final GestionarLogisticaLoteUseCase logistica;
    private final ConsultarLoteUseCase consultarLote;

    public LogisticaController(GestionarLogisticaLoteUseCase logistica, ConsultarLoteUseCase consultarLote) {
        this.logistica = logistica;
        this.consultarLote = consultarLote;
    }

    @PostMapping("/api/lotes/{id}/ingresos")
    @Operation(summary = "Ingresar el lote a un almacenamiento", description = "Reglas 9 y 13.")
    public ResponseEntity<Void> ingresar(@PathVariable UUID id, @Valid @RequestBody IngresoAlmacenRequest request) {
        logistica.ingresarAAlmacen(LoteId.de(id), AlmacenId.de(request.almacenId()), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/lotes/{id}/despachos/almacen")
    @Operation(summary = "Despachar hacia otro almacenamiento", description = "Reglas 10, 11, 13 y 16.")
    public ResponseEntity<Void> despacharAAlmacen(@PathVariable UUID id,
                                                  @Valid @RequestBody DespachoAlmacenRequest request) {
        logistica.despacharAAlmacen(LoteId.de(id), AlmacenId.de(request.almacenDestinoId()),
                Duration.ofHours(request.horasTransito()), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/lotes/{id}/despachos/comprador")
    @Operation(summary = "Despachar hacia el comprador", description = "Reglas 10, 11, 15 y 16.")
    public ResponseEntity<Void> despacharAComprador(@PathVariable UUID id,
                                                    @Valid @RequestBody DespachoCompradorRequest request) {
        logistica.despacharAComprador(LoteId.de(id), CompradorId.de(request.compradorId()),
                Duration.ofHours(request.horasTransito()), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/lotes/{id}/entrega")
    @Operation(summary = "Confirmar la entrega al comprador", description = "Cierra la cadena de custodia.")
    public ResponseEntity<Void> confirmarEntrega(@PathVariable UUID id, @Valid @RequestBody EntregaRequest request) {
        logistica.confirmarEntrega(LoteId.de(id), request.receptor(), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/almacenes/{almacenId}/orden-despacho")
    @Operation(summary = "Orden de despacho de un almacenamiento",
            description = "Regla 16 (FEFO): primero el lote que vence antes.")
    public List<LoteResumenResponse> ordenDeDespacho(@PathVariable UUID almacenId, @RequestParam TipoCultivo tipoCultivo) {
        return consultarLote.consultarOrdenDeDespacho(AlmacenId.de(almacenId), tipoCultivo).stream()
                .map(LoteResumenResponse::desde).toList();
    }
}
