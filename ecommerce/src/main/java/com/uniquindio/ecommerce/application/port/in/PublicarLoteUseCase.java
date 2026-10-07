package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;

import java.util.Optional;

/**
 * Caso de uso <b>Publicar producto</b> aplicado al lote.
 *
 * <p>Coordina la regla 1 (que vive fuera del lote, en el agregado Campesino) con
 * las reglas 2 y 8 (que vive dentro del lote). El caso de uso no decide nada del
 * negocio: pregunta al puerto de autorizacion, delega en el agregado y guarda.</p>
 */
public interface PublicarLoteUseCase {

    void publicar(PublicarLoteCommand comando);

    void desactivar(DesactivarLoteCommand comando);

    void reactivar(LoteId lote, CampesinoId solicitante);

    /** Regla 5: retira definitivamente. Falla si el lote tiene pedidos activos. */
    void retirar(LoteId lote, CampesinoId solicitante);

    /**
     * Devuelve el motivo por el que el lote no puede publicarse, sin lanzar
     * excepcion. Lo usa la interfaz para mostrarle al campesino que le falta.
     */
    Optional<String> diagnosticarPublicacion(LoteId lote);

    record PublicarLoteCommand(LoteId lote, CampesinoId solicitante) {
    }

    record DesactivarLoteCommand(LoteId lote, CampesinoId solicitante, String motivo) {
    }
}
