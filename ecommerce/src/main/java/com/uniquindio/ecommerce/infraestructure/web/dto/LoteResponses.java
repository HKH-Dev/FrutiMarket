package com.uniquindio.ecommerce.infraestructure.web.dto;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaLote;
import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase.VistaTrazabilidad;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Respuestas JSON. Convierten las vistas de los casos de uso a tipos simples
 * (UUID, BigDecimal, String) para que el frontend no dependa de los objetos de valor.
 */
public final class LoteResponses {

    private LoteResponses() {
    }

    public record LoteRegistradoResponse(UUID id) {
    }

    public record CantidadResponse(BigDecimal valor, String unidad) {

        public static CantidadResponse desde(Cantidad cantidad) {
            return new CantidadResponse(cantidad.valor(), cantidad.unidad().simbolo());
        }
    }

    public record CalidadResponse(String categoria, int porcentajeDefectos, boolean certificadoOrganico, int puntaje) {

        public static CalidadResponse desde(Calidad calidad) {
            return new CalidadResponse(calidad.categoria().name(), calidad.porcentajeDefectos(),
                    calidad.certificadoOrganico(), calidad.puntaje());
        }
    }

    public record LoteResumenResponse(UUID id, String codigo, String producto, UUID campesinoId,
                                      BigDecimal cantidadDisponible, String unidad, BigDecimal precioPorUnidad,
                                      int puntajeCalidad, Long diasVidaUtilRestante, String estado) {

        public static LoteResumenResponse desde(VistaLote vista) {
            return new LoteResumenResponse(vista.lote().valor(), vista.codigo(), vista.producto(),
                    vista.campesino().valor(), vista.cantidadDisponible().valor(),
                    vista.cantidadDisponible().unidad().simbolo(),
                    vista.precio() == null ? null : vista.precio().valorPorUnidad(),
                    vista.puntajeCalidad(), diasONulo(vista.diasVidaUtilRestante()), vista.estado().name());
        }
    }

    public record RegistroCustodiaResponse(String tipo, String descripcion, UUID almacenId, String lugar,
                                           String responsable, Instant momento, BigDecimal temperaturaC,
                                           String observacion) {

        public static RegistroCustodiaResponse desde(RegistroCustodia registro) {
            return new RegistroCustodiaResponse(registro.tipo().name(), registro.tipo().etiqueta(),
                    registro.almacen().map(AlmacenId::valor).orElse(null), registro.lugar(), registro.responsable(),
                    registro.momento(), registro.temperaturaC().orElse(null), registro.observacion());
        }
    }

    public record TrazabilidadResponse(UUID id, String codigo, String producto, UUID campesinoId, UUID fincaOrigen,
                                       String tecnicaProduccion, LocalDate fechaCosecha, LocalDate fechaLimiteConsumo,
                                       Long diasVidaUtilRestante, CalidadResponse calidadDeclarada,
                                       CalidadResponse calidadVerificada, List<RegistroCustodiaResponse> custodia) {

        public static TrazabilidadResponse desde(VistaTrazabilidad vista) {
            return new TrazabilidadResponse(vista.lote().valor(), vista.codigo(), vista.producto(),
                    vista.ficha().campesinoResponsable().valor(), vista.ficha().fincaOrigen(),
                    vista.ficha().tecnica().etiqueta(), vista.fechaCosecha(), vista.fechaLimiteConsumo().orElse(null),
                    diasONulo(vista.diasVidaUtilRestante()), CalidadResponse.desde(vista.calidadDeclarada()),
                    vista.calidadVerificada().map(CalidadResponse::desde).orElse(null),
                    vista.custodia().stream().map(RegistroCustodiaResponse::desde).toList());
        }
    }

    public record DiagnosticoPublicacionResponse(boolean publicable, String motivo) {
    }

    public record InspeccionResponse(boolean calidadCoincide) {
    }

    /** El dominio usa Long.MAX_VALUE para "no perecedero"; en JSON se envia null. */
    private static Long diasONulo(long dias) {
        return dias == Long.MAX_VALUE ? null : dias;
    }
}
