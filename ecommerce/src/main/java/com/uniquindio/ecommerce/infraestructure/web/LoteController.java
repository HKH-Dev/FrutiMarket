package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.dto.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.PublicarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.DesactivarRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.SolicitudCampesino;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.DiagnosticoPublicacionResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.LoteRegistradoResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.LoteResumenResponse;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.TrazabilidadResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/** Registro, consulta y publicacion de lotes. */
@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Registro, catalogo, trazabilidad y publicacion")
public class LoteController {

    private final RegistrarLoteUseCase registrarLote;
    private final ConsultarLoteUseCase consultarLote;
    private final PublicarLoteUseCase publicarLote;

    public LoteController(RegistrarLoteUseCase registrarLote, ConsultarLoteUseCase consultarLote,
                          PublicarLoteUseCase publicarLote) {
        this.registrarLote = registrarLote;
        this.consultarLote = consultarLote;
        this.publicarLote = publicarLote;
    }

    @PostMapping
    @Operation(summary = "Registrar un lote cosechado", description = "Queda en estado REGISTRADO hasta que se publique.")
    public ResponseEntity<LoteRegistradoResponse> registrar(@Valid @RequestBody RegistrarLoteRequest request) {
        LoteId id = registrarLote.registrar(request.aComando());
        var ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}/trazabilidad")
                .buildAndExpand(id.valor()).toUri();
        return ResponseEntity.created(ubicacion).body(new LoteRegistradoResponse(id.valor()));
    }

    @GetMapping
    @Operation(summary = "Lotes disponibles de un producto", description = "De todos los campesinos, del mejor al peor.")
    public List<LoteResumenResponse> disponiblesDe(@RequestParam UUID productoId) {
        return consultarLote.consultarDisponiblesDe(ProductoId.de(productoId)).stream()
                .map(LoteResumenResponse::desde).toList();
    }

    @GetMapping("/mejor")
    @Operation(summary = "Mejor lote disponible de un producto")
    public LoteResumenResponse mejorDe(@RequestParam UUID productoId) {
        return consultarLote.mejorLoteDe(ProductoId.de(productoId)).map(LoteResumenResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("lote disponible del producto", productoId));
    }

    @GetMapping("/campesino/{campesinoId}")
    @Operation(summary = "Lotes de un campesino", description = "En cualquier estado.")
    public List<LoteResumenResponse> delCampesino(@PathVariable UUID campesinoId) {
        return consultarLote.consultarPorCampesino(CampesinoId.de(campesinoId)).stream()
                .map(LoteResumenResponse::desde).toList();
    }

    @GetMapping("/{id}/trazabilidad")
    @Operation(summary = "Trazabilidad del lote", description = "Origen, calidad y cadena de custodia completa (regla 8).")
    public TrazabilidadResponse trazabilidad(@PathVariable UUID id) {
        return TrazabilidadResponse.desde(consultarLote.consultarTrazabilidad(LoteId.de(id)));
    }

    @GetMapping("/{id}/diagnostico-publicacion")
    @Operation(summary = "Por que el lote no puede publicarse", description = "No lanza error: devuelve el motivo, si existe.")
    public DiagnosticoPublicacionResponse diagnosticar(@PathVariable UUID id) {
        return publicarLote.diagnosticarPublicacion(LoteId.de(id))
                .map(motivo -> new DiagnosticoPublicacionResponse(false, motivo))
                .orElse(new DiagnosticoPublicacionResponse(true, null));
    }

    @PostMapping("/{id}/publicar")
    @Operation(summary = "Publicar en el catalogo", description = "Reglas 1, 2 y 8. Aplica la merma antes de publicar.")
    public ResponseEntity<Void> publicar(@PathVariable UUID id, @Valid @RequestBody SolicitudCampesino solicitud) {
        publicarLote.publicar(LoteId.de(id), CampesinoId.de(solicitud.campesinoId()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desactivar")
    @Operation(summary = "Sacar temporalmente de la venta", description = "Regla 5: no se borra; puede reactivarse.")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id, @Valid @RequestBody DesactivarRequest request) {
        publicarLote.desactivar(LoteId.de(id), CampesinoId.de(request.campesinoId()), request.motivo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivar")
    @Operation(summary = "Volver a publicar un lote desactivado")
    public ResponseEntity<Void> reactivar(@PathVariable UUID id, @Valid @RequestBody SolicitudCampesino solicitud) {
        publicarLote.reactivar(LoteId.de(id), CampesinoId.de(solicitud.campesinoId()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Retirar definitivamente", description = "Regla 5: falla con 422 si tiene pedidos activos.")
    public ResponseEntity<Void> retirar(@PathVariable UUID id, @RequestParam UUID campesinoId) {
        publicarLote.retirar(LoteId.de(id), CampesinoId.de(campesinoId));
        return ResponseEntity.noContent().build();
    }
}
