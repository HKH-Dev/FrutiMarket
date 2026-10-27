package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ControlarCalidadLoteUseCase;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.CorregirCalidadRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.IncidenciaRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.InspeccionRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.ResponsableRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteRequests.TemperaturaRequest;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.InspeccionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Controles de calidad y condiciones (reglas 11 y 18). */
@RestController
@RequestMapping("/api/lotes/{id}")
@Tag(name = "Calidad", description = "Inspecciones, temperatura, incidencias y revision")
public class CalidadController {

    private final ControlarCalidadLoteUseCase calidad;

    public CalidadController(ControlarCalidadLoteUseCase calidad) {
        this.calidad = calidad;
    }

    @PostMapping("/inspecciones")
    @Operation(summary = "Registrar una inspeccion de calidad",
            description = "Regla 18: si la observada es mucho peor que la declarada, el lote queda EN_REVISION.")
    public InspeccionResponse inspeccionar(@PathVariable UUID id, @Valid @RequestBody InspeccionRequest request) {
        return new InspeccionResponse(calidad.inspeccionarCalidad(LoteId.de(id), request.observada().aDominio(),
                request.inspector()));
    }

    @PostMapping("/temperaturas")
    @Operation(summary = "Registrar lectura de temperatura",
            description = "Regla 11: fuera del rango de conservacion rompe la cadena de frio.")
    public ResponseEntity<Void> registrarTemperatura(@PathVariable UUID id, @Valid @RequestBody TemperaturaRequest request) {
        calidad.registrarTemperatura(LoteId.de(id), request.temperaturaC(), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/incidencias")
    @Operation(summary = "Reportar una incidencia", description = "Regla 11: el lote queda detenido hasta validarse.")
    public ResponseEntity<Void> reportarIncidencia(@PathVariable UUID id, @Valid @RequestBody IncidenciaRequest request) {
        calidad.reportarIncidencia(LoteId.de(id), request.detalle(), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revision/validar")
    @Operation(summary = "Validar un lote en revision", description = "Vuelve a su estado anterior.")
    public ResponseEntity<Void> validarRevision(@PathVariable UUID id, @Valid @RequestBody ResponsableRequest request) {
        calidad.validarRevision(LoteId.de(id), request.responsable());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/calidad-declarada")
    @Operation(summary = "Corregir la calidad declarada", description = "Regla 18: solo el campesino responsable.")
    public ResponseEntity<Void> corregir(@PathVariable UUID id, @Valid @RequestBody CorregirCalidadRequest request) {
        calidad.corregirCalidadDeclarada(LoteId.de(id), CampesinoId.de(request.campesinoId()),
                request.calidad().aDominio());
        return ResponseEntity.noContent().build();
    }
}
