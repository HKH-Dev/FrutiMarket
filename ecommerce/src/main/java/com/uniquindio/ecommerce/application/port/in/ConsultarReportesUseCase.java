package com.uniquindio.ecommerce.application.port.in;

import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.ProductoVendido;

import java.util.List;

/** Caso de uso <b>Consultar reportes</b>: informacion para el administrador de la plataforma. */
public interface ConsultarReportesUseCase {

    /** Productos mas vendidos; {@code limite} entre 1 y 50. */
    List<ProductoVendido> productosMasVendidos(int limite);
}
