package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

/**
 * Casos de uso <b>Gestionar disponibilidad</b> y <b>Registrar merma</b>.
 *
 * <p>Aqui vive la regla 3: un pedido no puede reservar mas de lo disponible. La
 * comprobacion la hace el agregado; el caso de uso solo garantiza que se ejecute
 * dentro de una transaccion y sobre una version fresca del lote.</p>
 */
public interface GestionarDisponibilidadUseCase {

    /** Regla 3: compromete cantidad para un pedido. */
    void reservar(ReservarCantidadCommand comando);

    /** Devuelve la cantidad de un pedido anulado. */
    void liberar(LoteId lote, PedidoId pedido);

    /** Marca la salida fisica de lo reservado. */
    void confirmarSalida(LoteId lote, PedidoId pedido);

    /** Caso de uso <b>Registrar merma</b>: descuenta la perdida esperada. */
    void registrarMerma(RegistrarMermaCommand comando);

    /** Consulta la cantidad que queda libre en el lote. */
    Cantidad consultarDisponible(LoteId lote);

    record ReservarCantidadCommand(LoteId lote, PedidoId pedido, Cantidad cantidad) {
    }

    record RegistrarMermaCommand(LoteId lote, CampesinoId solicitante, Merma merma) {
    }
}
