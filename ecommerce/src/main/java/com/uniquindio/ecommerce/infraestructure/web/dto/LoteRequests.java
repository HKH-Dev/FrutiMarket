package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/** Cuerpos de las peticiones sobre un lote ya registrado. */
public final class LoteRequests {

    private LoteRequests() {
    }

    @Schema(description = "Campesino que solicita la operacion; debe ser el responsable del lote (regla 5).")
    public record SolicitudCampesino(@NotNull UUID campesinoId) {
    }

    public record DesactivarRequest(@NotNull UUID campesinoId,
                                    @Schema(example = "Lluvias retrasaron la recoleccion") String motivo) {
    }

    public record MermaRequest(@NotNull UUID campesinoId,
                               @NotNull @DecimalMin("0") @DecimalMax("40") BigDecimal porcentaje,
                               @Schema(example = "Golpes durante la cosecha") String justificacion) {
    }

    @Schema(description = "Una calidad declarada u observada; el puntaje se calcula en el dominio.")
    public record CalidadRequest(@NotNull Calidad.Categoria categoria,
                                 @Min(0) @Max(100) int porcentajeDefectos,
                                 boolean certificadoOrganico) {

        public Calidad aDominio() {
            return Calidad.de(categoria, porcentajeDefectos, certificadoOrganico);
        }
    }

    public record CorregirCalidadRequest(@NotNull UUID campesinoId, @NotNull CalidadRequest calidad) {
    }

    @Schema(description = "Reserva de un pedido; la cantidad va en la unidad del lote.")
    public record ReservaRequest(@NotNull UUID pedidoId, @NotNull @Positive BigDecimal cantidad) {
    }

    public record InspeccionRequest(@NotNull CalidadRequest observada,
                                    @NotBlank @Schema(example = "Inspector ICA") String inspector) {
    }

    public record TemperaturaRequest(@NotNull @Schema(example = "6.5") BigDecimal temperaturaC,
                                     @NotBlank String responsable) {
    }

    public record IncidenciaRequest(@NotBlank @Schema(example = "Canastilla rota en el cargue") String detalle,
                                    @NotBlank String responsable) {
    }

    public record ResponsableRequest(@NotBlank @Schema(example = "Coordinador de acopio") String responsable) {
    }

    public record IngresoAlmacenRequest(@NotNull UUID almacenId, @NotBlank String responsable) {
    }

    public record DespachoAlmacenRequest(@NotNull UUID almacenDestinoId,
                                         @Positive @Schema(example = "6") long horasTransito,
                                         @NotBlank String responsable) {
    }

    public record DespachoCompradorRequest(@NotNull UUID compradorId,
                                           @Positive @Schema(example = "4") long horasTransito,
                                           @NotBlank String responsable) {
    }

    public record EntregaRequest(@Schema(example = "Ana, jefe de cocina") String receptor,
                                 @NotBlank String responsable) {
    }
}
