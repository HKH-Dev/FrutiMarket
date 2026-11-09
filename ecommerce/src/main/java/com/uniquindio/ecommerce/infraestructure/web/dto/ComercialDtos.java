package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.application.dto.CompraResponse;
import com.uniquindio.ecommerce.application.dto.RealizarCompraRequest;
import com.uniquindio.ecommerce.application.port.in.ChatUseCase.VistaConversacion;
import com.uniquindio.ecommerce.application.port.in.PagarCompraUseCase.Comprobante;
import com.uniquindio.ecommerce.application.port.in.RecomendarProductosUseCase.Sugerencia;
import com.uniquindio.ecommerce.domain.chat.Mensaje;
import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.comercial.DescuentoTemporada;
import com.uniquindio.ecommerce.domain.comercial.Liquidacion;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.PagoRepository.ResumenPagos;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.VentaDeCampesino;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.infraestructure.web.dto.LoteResponses.LoteResumenResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Peticiones y respuestas de descuentos, cupones, pagos, recomendaciones, chat y paneles. */
public final class ComercialDtos {

    private ComercialDtos() {
    }

    // --- Descuentos por temporada ---

    public record CrearDescuentoRequest(@NotBlank @Size(max = 80) @Schema(example = "Cosecha de mango") String nombre,
                                        @Schema(description = "Vacio: aplica a todo el catalogo") UUID productoId,
                                        @Min(1) @Max(70) @Schema(example = "15") int porcentaje,
                                        @NotNull LocalDate inicio, @NotNull LocalDate fin) {
    }

    public record DescuentoResponse(UUID id, String nombre, UUID productoId, int porcentaje, LocalDate inicio,
                                    LocalDate fin, boolean activo) {
        public static DescuentoResponse desde(DescuentoTemporada d) {
            return new DescuentoResponse(d.id(), d.nombre(), d.producto().map(ProductoId::valor).orElse(null),
                    d.porcentaje(), d.inicio(), d.fin(), d.activo());
        }
    }

    // --- Cupones ---

    public record CrearCuponRequest(@NotBlank @Size(max = 30) @Schema(example = "CLIENTE-FIEL") String codigo,
                                    @NotNull UUID compradorId, @Min(1) @Max(50) @Schema(example = "10") int porcentaje,
                                    @NotNull LocalDate venceEl) {
    }

    public record CuponResponse(UUID id, String codigo, UUID compradorId, int porcentaje, LocalDate venceEl, Instant usadoEn) {
        public static CuponResponse desde(Cupon c) {
            return new CuponResponse(c.id(), c.codigo(), c.comprador().valor(), c.porcentaje(), c.venceEl(), c.usadoEn().orElse(null));
        }
    }

    // --- Pagos ---

    public record CotizacionRequest(@NotEmpty List<RealizarCompraRequest.@Valid Item> items, @Size(max = 30) String cupon) {
    }

    public record CheckoutRequest(@NotEmpty List<RealizarCompraRequest.@Valid Item> items, @Size(max = 30) String cupon,
                                  @NotNull Pago.Metodo metodo,
                                  @Pattern(regexp = "\\d{4}", message = "Deben ser 4 digitos")
                                  @Schema(description = "Solo tarjeta. Nunca el numero completo. 0000 simula un rechazo.", example = "4242")
                                  String ultimosDigitos) {
    }

    public record LiquidacionResponse(BigDecimal subtotal, BigDecimal descuentoTemporada, BigDecimal descuentoCupon, BigDecimal total) {
        public static LiquidacionResponse desde(Liquidacion l) {
            return new LiquidacionResponse(l.subtotal(), l.descuentoTemporada(), l.descuentoCupon(), l.total());
        }
    }

    public record ComprobanteResponse(CompraResponse compra, UUID pagoId, String estadoPago, String referencia, Pago.Metodo metodo,
                                      LiquidacionResponse liquidacion) {
        public static ComprobanteResponse desde(Comprobante c) {
            return new ComprobanteResponse(c.compra(), c.pago().id(), c.pago().estado().name(), c.pago().referencia(),
                    c.pago().metodo(), LiquidacionResponse.desde(c.pago().liquidacion()));
        }
    }

    // --- Recomendaciones ---

    public record SugerenciaResponse(String motivo, int descuento, LoteResumenResponse lote) {
        public static SugerenciaResponse desde(Sugerencia s) {
            return new SugerenciaResponse(s.motivo(), s.descuento(), LoteResumenResponse.desde(s.mejorLote()));
        }
    }

    // --- Chat ---

    public record IniciarConversacionRequest(@NotNull UUID loteId) {
    }

    public record EnviarMensajeRequest(@NotBlank @Size(max = 1000) String texto) {
    }

    public record ConversacionResponse(UUID id, UUID compradorId, String comprador, UUID campesinoId, String campesino,
                                       String ultimoMensaje, Instant ultimaActividad) {
        public static ConversacionResponse desde(VistaConversacion v) {
            return new ConversacionResponse(v.id(), v.compradorId(), v.comprador(), v.campesinoId(), v.campesino(), v.ultimoMensaje(), v.ultimaActividad());
        }
    }

    public record MensajeResponse(UUID id, UUID conversacionId, UUID autorId, String texto, Instant momento) {
        public static MensajeResponse desde(Mensaje m) {
            return new MensajeResponse(m.id(), m.conversacion(), m.autor(), m.texto(), m.momento());
        }
    }

    // --- Paneles ---

    public record ResumenPagosResponse(BigDecimal ingresos, BigDecimal descuentos, long aprobados, long reembolsados) {
        public static ResumenPagosResponse desde(ResumenPagos r) {
            return new ResumenPagosResponse(r.ingresos(), r.descuentos(), r.aprobados(), r.reembolsados());
        }
    }

    public record CampesinoResponse(UUID id, String nombre, String numeroIdentificacion, boolean autorizado) {
        public static CampesinoResponse desde(Campesino c) {
            return new CampesinoResponse(c.getId().valor(), c.getNombre(), c.getNumeroIdentificacion(), c.estaAutorizadoParaPublicar());
        }
    }

    public record CompradorResponse(UUID id, String nombre, String tipo, String municipio) {
        public static CompradorResponse desde(Comprador c) {
            return new CompradorResponse(c.getId().valor(), c.getNombre(), c.getTipo().name(), c.getMunicipio());
        }
    }

    public record VentaResponse(UUID productoId, String nombre, BigDecimal cantidadVendida, String unidad, BigDecimal ingresos, long numeroCompras) {
        public static VentaResponse desde(VentaDeCampesino v) {
            return new VentaResponse(v.producto().valor(), v.nombre(), v.cantidadVendida(), v.unidad().simbolo(), v.ingresos(), v.numeroCompras());
        }
    }
}
