package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PedidoId;

/**
 * Casos de uso <b>Gestionar disponibilidad</b> y <b>Registrar merma</b>. La regla 3 la
 * comprueba el agregado; el caso de uso solo carga, ejecuta y guarda.
 */
public interface GestionarDisponibilidadUseCase {

    void reservar(LoteId lote, PedidoId pedido, Cantidad cantidad);

    void liberar(LoteId lote, PedidoId pedido);

    void confirmarSalida(LoteId lote, PedidoId pedido);

    void registrarMerma(LoteId lote, CampesinoId solicitante, Merma merma);

    Cantidad consultarDisponible(LoteId lote);
}
