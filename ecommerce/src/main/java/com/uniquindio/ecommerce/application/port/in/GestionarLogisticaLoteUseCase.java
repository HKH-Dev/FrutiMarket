package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CentroRedistribucionId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.DestinatarioId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.*;

/**
 * Casos de uso <b>Registrar punto de acopio</b>, <b>Solicitar entrega</b> y
 * <b>Gestionar ruta de entrega</b>.
 *
 * <p>Es el caso de uso donde se cruzan las reglas que pertenecen al lote (9 a 12,
 * 17) con las que pertenecen al centro de redistribucion (13, 15) y al pedido (14).
 * El reparto es deliberado: el caso de uso pregunta a los puertos por lo que no es
 * suyo y deja que el agregado valide lo que si lo es.</p>
 */
public interface GestionarLogisticaLoteUseCase {

    /** Regla 9: declara la condicion de conservacion antes de mover el lote. */
    void declararCondicionConservacion(LoteId lote, CondicionConservacion condicion);

    /** Regla 9: envia el lote a un punto de acopio. */
    void enviarAPuntoAcopio(LoteId lote, PuntoAcopioId puntoAcopio);

    /**
     * Reglas 10 a 14 y 17: despacha el lote hacia un centro de redistribucion.
     * Comprueba la habilitacion del centro (13) y la ventana de entrega (14) antes
     * de pedir al agregado que valide lo suyo.
     */
    void despacharACentro(DespacharLoteCommand comando);

    /** Regla 15: confirma la entrega al destinatario si su direccion esta cubierta. */
    void confirmarEntrega(ConfirmarEntregaCommand comando);

    record DespacharLoteCommand(LoteId lote,
                                CentroRedistribucionId centroDestino,
                                TipoEmpaque empaque,
                                TiempoTransito tiempoTransito,
                                VentanaEntrega ventanaEntrega) {
    }

    record ConfirmarEntregaCommand(LoteId lote,
                                   DestinatarioId destinatario,
                                   CentroRedistribucionId centroOrigen,
                                   Ubicacion direccionEntrega) {
    }
}
