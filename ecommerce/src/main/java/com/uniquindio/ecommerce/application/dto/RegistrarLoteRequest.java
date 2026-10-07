package com.uniquindio.ecommerce.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;

/**
 * Request: el campesino registra un lote de materia prima.
 * Mapea a {@code Lote.registrarMateriaPrima(...)}.
 *
 * <p>No trae {@code id} (lo genera el dominio con {@code LoteId.nuevo()}), ni estado
 * (todo lote nace en BORRADOR), ni momento de registro (lo pone el puerto {@code Reloj}
 * del servidor, para que el cliente no pueda falsear fechas).</p>
 *
 * @param codigo             codigo con el que el campesino reconoce su lote; el dominio exige que no este vacio (INV-CODIGO).
 * @param productoId         UUID del producto del catalogo; el lote referencia al producto solo por id.
 * @param campesinoId        UUID del campesino responsable (regla 8). En produccion debe salir del usuario autenticado, no del body.
 * @param fincaOrigenId      UUID de la finca; junto con la tecnica arma la {@code FichaTrazabilidad} (regla 8).
 * @param tecnicaProduccion  nombre de {@code TecnicaProduccion}; obligatorio en la ficha de trazabilidad.
 * @param tipoCultivo        nombre de {@code TipoCultivo}; decide si el lote es perecedero y agrupa el FEFO (reglas 13 y 16).
 * @param cantidad           cantidad cosechada; con {@code unidadMedida} forma el VO {@code Cantidad} (regla 2: no puede ser cero).
 * @param unidadMedida       nombre de {@code UnidadMedida}; sin ella la cantidad no tiene sentido (no se restan kg con canastillas).
 * @param precioPorUnidad    precio de finca por unidad; sin precio el lote no puede publicarse (regla 2).
 * @param moneda             nombre de {@code Moneda} (COP, USD); define la escala del precio.
 * @param fechaCosecha       fecha de cosecha; no puede ser futura (INV-COSECHA) y es la base de la vida util.
 * @param vidaUtilDias       dias que dura el producto desde la cosecha; calcula la fecha limite de consumo (FEFO). 0 = no perecedero.
 * @param porcentajeMerma    perdida esperada (0 a 40); descuenta cantidad antes de publicar. Puede ser null (sin merma).
 * @param temporadaInicio    inicio de la temporada de cosecha (mes-dia); null junto con {@code temporadaFin} = todo el ano.
 * @param temporadaFin       fin de la temporada de cosecha; el lote no se publica fuera de ella.
 */
public record RegistrarLoteRequest(
        String codigo,
        String productoId,
        String campesinoId,
        String fincaOrigenId,
        String tecnicaProduccion,
        String tipoCultivo,
        BigDecimal cantidad,
        String unidadMedida,
        BigDecimal precioPorUnidad,
        String moneda,
        LocalDate fechaCosecha,
        int vidaUtilDias,
        BigDecimal porcentajeMerma,
        MonthDay temporadaInicio,
        MonthDay temporadaFin) {
}
