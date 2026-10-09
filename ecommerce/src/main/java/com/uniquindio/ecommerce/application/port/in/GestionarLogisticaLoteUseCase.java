package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.AlmacenId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.time.Duration;

/**
 * Movimiento fisico del lote: finca -> punto de acopio -> centro de redistribucion -> comprador.
 * Cada paso agrega un registro a la cadena de custodia.
 */
public interface GestionarLogisticaLoteUseCase {

    /** Reglas 9 y 13: el lote entra a un almacenamiento (desde la finca o al llegar de un transito). */
    void ingresarAAlmacen(LoteId lote, AlmacenId almacen, String responsable);

    /** Reglas 10, 11, 13 y 16: traslado entre almacenamientos. */
    void despacharAAlmacen(LoteId lote, AlmacenId destino, Duration tiempoTransito, String responsable);

    /** Reglas 10, 11, 15 y 16: salida hacia el comprador. */
    void despacharAComprador(LoteId lote, CompradorId comprador, Duration tiempoTransito, String responsable);

    void confirmarEntrega(LoteId lote, String receptor, String responsable);
}
