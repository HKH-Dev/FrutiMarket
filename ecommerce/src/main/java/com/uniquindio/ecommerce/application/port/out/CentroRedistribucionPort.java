package com.uniquindio.ecommerce.application.port.out;

import com.uniquindio.ecommerce.domain.valueobject.identidad.CentroRedistribucionId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.Ubicacion;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

/**
 * Puerto de salida hacia el agregado {@code CentroRedistribucion}.
 *
 * <p>Cubre las reglas que relacionan al lote con un centro pero que pertenecen al
 * centro, no al lote:</p>
 * <ul>
 *   <li><b>Regla 13:</b> un centro solo puede recibir los cultivos para los que esta habilitado.</li>
 *   <li><b>Regla 15:</b> la direccion del destinatario debe estar dentro de la zona de cobertura.</li>
 * </ul>
 */
public interface CentroRedistribucionPort {

    /** Regla 13. */
    boolean estaHabilitadoParaCultivo(CentroRedistribucionId centro, TipoCultivo tipoCultivo);

    /** Regla 15. */
    boolean cubreDireccion(CentroRedistribucionId centro, Ubicacion direccionDestinatario);

    /** Nombre legible del centro, para dejarlo como destino en la guia de despacho. */
    String nombreDe(CentroRedistribucionId centro);
}
