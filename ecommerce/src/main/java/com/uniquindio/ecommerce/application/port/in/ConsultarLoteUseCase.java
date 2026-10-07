package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.HitoDespacho;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.SelloOrigen;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso <b>Consultar trazabilidad</b> y <b>Consultar productos</b>.
 *
 * <p>Las consultas devuelven <i>vistas</i> (records planos), no el agregado. Es
 * deliberado: exponer el {@code Lote} a la capa web dejaria que un controlador
 * invocara {@code despachar()} por accidente. La vista lleva solo lo que el
 * comprador necesita ver.</p>
 */
public interface ConsultarLoteUseCase {

    /** Caso de uso Consultar trazabilidad: el origen completo que el comprador quiere ver. */
    VistaTrazabilidad consultarTrazabilidad(LoteId lote);

    /** Lotes publicados y con existencia de un producto del catalogo. */
    List<VistaLoteDisponible> consultarDisponiblesDe(ProductoId producto);

    List<VistaLoteDisponible> consultarPorCampesino(CampesinoId campesino);

    /** Regla 16: lotes de un punto de acopio ordenados por prioridad de despacho. */
    List<VistaLoteDisponible> consultarOrdenDeDespacho(PuntoAcopioId puntoAcopio, TipoCultivo tipoCultivo);

    /**
     * Vista de trazabilidad: es la propuesta de valor del marketplace convertida en
     * datos, el origen que permite al comprador saltarse la cadena de intermediacion.
     */
    record VistaTrazabilidad(LoteId lote,
                             String codigo,
                             FichaTrazabilidad ficha,
                             Optional<SelloOrigen> selloOrigen,
                             String denominacionOrigen,
                             LocalDate fechaCosecha,
                             Optional<LocalDate> fechaLimiteConsumo,
                             long diasVidaUtilRestante,
                             List<HitoDespacho> recorrido) {
    }

    /** Vista de catalogo: lo minimo para listar un lote disponible. */
    record VistaLoteDisponible(LoteId lote,
                               String codigo,
                               ProductoId producto,
                               Cantidad cantidadDisponible,
                               EstadoLote estado,
                               long diasVidaUtilRestante,
                               boolean tieneSelloVigente) {
    }
}
