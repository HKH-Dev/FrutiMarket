package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.*;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.FichaTrazabilidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TemporadaCosecha;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;

/**
 * Caso de uso <b>Registrar lote</b>.
 *
 * <p>Puerto de entrada. El controlador REST depende de esta interfaz, nunca de la
 * implementacion, para que el dominio pueda probarse sin levantar la aplicacion.</p>
 *
 * <p>Los comandos son records: llevan los datos de la peticion ya convertidos a
 * tipos del dominio. La conversion desde el JSON crudo es trabajo del adaptador de
 * entrada, no del caso de uso.</p>
 */
public interface RegistrarLoteUseCase {

    LoteId registrarMateriaPrima(RegistrarMateriaPrimaCommand comando);

    LoteId registrarTransformado(RegistrarTransformadoCommand comando);

    /** Datos para registrar un lote de materia prima. */
    record RegistrarMateriaPrimaCommand(String codigo,
                                        ProductoId producto,
                                        CampesinoId campesino,
                                        FichaTrazabilidad fichaTrazabilidad,
                                        TipoCultivo tipoCultivo,
                                        Cantidad cantidadInicial,
                                        PrecioFinca precioFinca,
                                        Calibre calibre,
                                        TemporadaCosecha temporada,
                                        LocalDate fechaCosecha,
                                        int vidaUtilDias,
                                        Merma merma) {
    }

    /** Datos para registrar un lote de producto transformado. */
    record RegistrarTransformadoCommand(String codigo,
                                        ProductoId producto,
                                        CampesinoId campesino,
                                        FichaTrazabilidad fichaTrazabilidad,
                                        TipoCultivo tipoCultivo,
                                        Cantidad cantidadInicial,
                                        PrecioFinca precioFinca,
                                        FichaTransformacion fichaTransformacion,
                                        Merma merma) {
    }
}
