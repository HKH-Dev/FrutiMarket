package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.valueobject.catalogo.Cantidad;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.Merma;
import com.uniquindio.ecommerce.domain.valueobject.catalogo.PrecioFinca;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.ProductoId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.Calidad;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TecnicaProduccion;

import java.time.LocalDate;
import java.util.UUID;

/** Caso de uso <b>Registrar lote</b>: un campesino carga lo que cosecho o produjo. */
public interface RegistrarLoteUseCase {

    LoteId registrar(RegistrarLoteCommand comando);

    record RegistrarLoteCommand(String codigo,
                                ProductoId producto,
                                CampesinoId campesino,
                                UUID fincaOrigen,
                                TecnicaProduccion tecnica,
                                Calidad calidad,
                                Cantidad cantidad,
                                PrecioFinca precioFinca,
                                LocalDate fechaCosecha,
                                Merma merma) {
    }
}
