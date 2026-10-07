package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Calibre;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.MedicionCalibre;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.logistica.TipoHito;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.SelloOrigen;

import java.math.BigDecimal;

/**
 * Casos de uso de control de calidad sobre el lote en ruta.
 *
 * <p>Agrupa las reglas 11, 17 y 18, que comparten el mismo desenlace: el lote queda
 * detenido en revision y no continua hasta que alguien lo valide o corrija el dato
 * de origen.</p>
 */
public interface ControlarCalidadLoteUseCase {

    /** Anota un paso del recorrido; si trae temperatura, evalua la regla 11. */
    void registrarHito(RegistrarHitoCommand comando);

    /** Regla 11: reporte explicito de ruptura de la cadena de frio. */
    void reportarRupturaCadenaFrio(LoteId lote, String motivo);

    /** Regla 18: contrasta la medicion real contra el calibre declarado. */
    boolean verificarCalibre(LoteId lote, MedicionCalibre medicion);

    /** Regla 11: libera un lote detenido tras la validacion manual. */
    void validarLoteEnRevision(LoteId lote, String responsable);

    /** Regla 18: corrige el calibre declarado y libera la revision. */
    void corregirCalibre(LoteId lote, Calibre calibreReal, String responsable);

    /** Regla 17: renueva la certificacion de origen vencida. */
    void renovarCertificacion(LoteId lote, SelloOrigen nuevoSello, String responsable);

    record RegistrarHitoCommand(LoteId lote, TipoHito tipo, String ubicacion, BigDecimal temperaturaC) {
    }
}
