package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.PagoRepository.ResumenPagos;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.VentaDeCampesino;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;

import java.util.List;

/** Lecturas de los paneles del administrador y del campesino. */
public interface ConsultarPanelesUseCase {

    ResumenPagos resumenPagos();

    List<Campesino> campesinos();

    List<Comprador> compradores();

    List<VentaDeCampesino> ventasDe(CampesinoId campesino);
}
