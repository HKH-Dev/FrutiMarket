package com.uniquindio.ecommerce.infraestructure.web;

import java.math.BigDecimal;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.uniquindio.ecommerce.infraestructure.web.dto.PaginaResponse;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;
import com.uniquindio.ecommerce.domain.consulta.BusquedaLotes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import com.uniquindio.ecommerce.infraestructure.seguridad.UsuarioActual;
import com.uniquindio.ecommerce.application.dto.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.PublicarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.DesactivarRequest;
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Registrar un lote cosechado (CAMPESINO)",
            description = "Queda en estado REGISTRADO hasta que se publique. El campesino es el del token.")
    public ResponseEntity<LoteRegistradoResponse> registrar(@Valid @RequestBody RegistrarLoteRequest request,
                                                            @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        LoteId id = registrarLote.registrar(request.aComando(UsuarioActual.campesino(token)));
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

    @GetMapping("/buscar")
    @Operation(summary = "Buscar en el catalogo (paginado)",
            description = "Lotes publicados con cantidad disponible. Busca el texto en el nombre del producto y en el "
                    + "codigo del lote, con filtros opcionales por tipo de cultivo y precio maximo. Los lotes retirados "
                    + "(borrado logico) nunca aparecen. Ordena por publicacion mas reciente.")
    public PaginaResponse<LoteResumenResponse> buscar(
            @RequestParam(required = false) @Parameter(example = "mango") String texto,
            @RequestParam(required = false) TipoCultivo tipoCultivo,
            @RequestParam(required = false) @Positive @Parameter(example = "5000") BigDecimal precioMaximo,
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {
        return PaginaResponse.desde(
                consultarLote.buscarDisponibles(new BusquedaLotes(texto, tipoCultivo, precioMaximo, pagina, tamano)),
                LoteResumenResponse::desde);
    }

    @GetMapping("/mejor")
    @Operation(summary = "Mejor lote disponible de un producto")
    public LoteResumenResponse mejorDe(@RequestParam UUID productoId) {
        return consultarLote.mejorLoteDe(ProductoId.de(productoId)).map(LoteResumenResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("lote disponible del producto", productoId));
    }

    @GetMapping("/mios")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mis lotes (CAMPESINO)", description = "Los lotes del campesino del token, en cualquier estado.")
    public List<LoteResumenResponse> misLotes(@Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        return consultarLote.consultarPorCampesino(UsuarioActual.campesino(token)).stream()
                .map(LoteResumenResponse::desde).toList();
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publicar en el catalogo (CAMPESINO dueno)", description = "Reglas 1, 2, 5 y 8. Aplica la merma antes de publicar.")
    public ResponseEntity<Void> publicar(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        publicarLote.publicar(LoteId.de(id), UsuarioActual.campesino(token));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desactivar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sacar temporalmente de la venta (CAMPESINO dueno)", description = "Regla 5: no se borra; puede reactivarse.")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id, @Valid @RequestBody DesactivarRequest request,
                                           @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        publicarLote.desactivar(LoteId.de(id), UsuarioActual.campesino(token), request.motivo());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Volver a publicar un lote desactivado (CAMPESINO dueno)")
    public ResponseEntity<Void> reactivar(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        publicarLote.reactivar(LoteId.de(id), UsuarioActual.campesino(token));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retirar definitivamente (CAMPESINO dueno)",
            description = "Borrado logico: el lote pasa a RETIRADO y no se elimina la fila. Regla 5: falla con 422 si tiene pedidos activos.")
    public ResponseEntity<Void> retirar(@PathVariable UUID id, @Parameter(hidden = true) @AuthenticationPrincipal Jwt token) {
        publicarLote.retirar(LoteId.de(id), UsuarioActual.campesino(token));
        return ResponseEntity.noContent().build();
    }
}
