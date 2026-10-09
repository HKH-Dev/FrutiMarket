package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Optional;

/**
 * Caso de uso <b>Publicar producto</b> aplicado al lote. Coordina la regla 1 (fuera del
 * lote, en el campesino) con las reglas 2 y 8 (dentro del lote).
 */
public interface PublicarLoteUseCase {

    void publicar(LoteId lote, CampesinoId solicitante);

    void desactivar(LoteId lote, CampesinoId solicitante, String motivo);

    void reactivar(LoteId lote, CampesinoId solicitante);

    /** Regla 5: retiro definitivo. Falla si el lote tiene pedidos activos. */
    void retirar(LoteId lote, CampesinoId solicitante);

    /** Motivo por el que el lote no puede publicarse, sin lanzar excepcion. */
    Optional<String> diagnosticarPublicacion(LoteId lote);
}
