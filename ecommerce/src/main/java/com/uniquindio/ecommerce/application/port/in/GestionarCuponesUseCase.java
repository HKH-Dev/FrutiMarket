package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;

import java.time.LocalDate;
import java.util.List;

public interface GestionarCuponesUseCase {

    Cupon crear(String codigo, CompradorId comprador, int porcentaje, LocalDate venceEl);

    List<Cupon> todos();

    List<Cupon> delComprador(CompradorId comprador);
}
