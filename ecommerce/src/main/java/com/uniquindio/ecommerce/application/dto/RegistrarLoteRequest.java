package com.uniquindio.ecommerce.application.dto;

import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase.RegistrarLoteCommand;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.UnidadMedida;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request: el formulario con el que el campesino carga su cosecha. Pocos campos y
 * sencillos: no se le pide humedad ni temperatura, solo lo necesario para saber que
 * tan bueno es su lote frente a los demas. Mapea a {@code RegistrarLoteUseCase.registrar(...)}.
 *
 * @param codigo              codigo con el que el campesino reconoce su lote (unico).
 * @param productoId          producto del catalogo (mango tommy, mora, mermelada de mora...).
 * @param campesinoId         campesino que vende (regla 1). En produccion sale del usuario autenticado.
 * @param fincaOrigenId       finca donde se cosecho; inicia la trazabilidad (regla 8).
 * @param tecnicaProduccion   organica, agroecologica, tradicional...
 * @param categoriaCalidad    extra, primera, segunda o industrial.
 * @param porcentajeDefectos  porcentaje aproximado de producto golpeado o manchado (0 a 100).
 * @param certificadoOrganico si tiene certificacion organica; suma puntaje.
 * @param cantidad            cantidad cosechada; no puede ser cero (regla 2).
 * @param unidadMedida        debe ser la unidad de venta del producto.
 * @param precioPorUnidad     precio de finca; sin precio el lote no se publica (regla 2).
 * @param fechaCosecha        cosecha o elaboracion; no puede ser futura.
 * @param porcentajeMerma     perdida esperada (0 a 40 %); null = sin merma.
 */
public record RegistrarLoteRequest(
        String codigo,
        UUID productoId,
        UUID campesinoId,
        UUID fincaOrigenId,
        TecnicaProduccion tecnicaProduccion,
        Calidad.Categoria categoriaCalidad,
        int porcentajeDefectos,
        boolean certificadoOrganico,
        BigDecimal cantidad,
        UnidadMedida unidadMedida,
        BigDecimal precioPorUnidad,
        LocalDate fechaCosecha,
        BigDecimal porcentajeMerma) {

    public RegistrarLoteCommand aComando() {
        return new RegistrarLoteCommand(
                codigo,
                ProductoId.de(productoId),
                CampesinoId.de(campesinoId),
                fincaOrigenId,
                tecnicaProduccion,
                Calidad.de(categoriaCalidad, porcentajeDefectos, certificadoOrganico),
                Cantidad.de(cantidad, unidadMedida),
                PrecioFinca.de(precioPorUnidad, unidadMedida),
                fechaCosecha,
                porcentajeMerma == null ? Merma.ninguna() : Merma.de(porcentajeMerma, "Declarada al registrar"));
    }
}
