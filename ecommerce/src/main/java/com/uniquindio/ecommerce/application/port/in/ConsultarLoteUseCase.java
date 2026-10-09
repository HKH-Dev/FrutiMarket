package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.catalogo.RegistroCustodia;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Casos de uso <b>Consultar trazabilidad</b> y <b>Consultar productos</b>. Devuelven vistas
 * (records planos), nunca el agregado, para que la capa web no invoque su comportamiento.
 */
public interface ConsultarLoteUseCase {

    /** Origen, calidad y cadena de custodia completa del lote. */
    VistaTrazabilidad consultarTrazabilidad(LoteId lote);

    /** Lotes disponibles de un producto, de todos los campesinos, del mejor al peor. */
    List<VistaLote> consultarDisponiblesDe(ProductoId producto);

    Optional<VistaLote> mejorLoteDe(ProductoId producto);

    List<VistaLote> consultarPorCampesino(CampesinoId campesino);

    /** Regla 16: lotes de un almacenamiento en el orden en que deben despacharse. */
    List<VistaLote> consultarOrdenDeDespacho(AlmacenId almacen, TipoCultivo tipoCultivo);

    record VistaTrazabilidad(LoteId lote,
                             String codigo,
                             String producto,
                             FichaTrazabilidad ficha,
                             Calidad calidadDeclarada,
                             Optional<Calidad> calidadVerificada,
                             LocalDate fechaCosecha,
                             Optional<LocalDate> fechaLimiteConsumo,
                             long diasVidaUtilRestante,
                             List<RegistroCustodia> custodia) {
    }

    record VistaLote(LoteId lote,
                     String codigo,
                     String producto,
                     CampesinoId campesino,
                     Cantidad cantidadDisponible,
                     PrecioFinca precio,
                     int puntajeCalidad,
                     long diasVidaUtilRestante,
                     EstadoLote estado) {
    }
}
