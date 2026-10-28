package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarReportesUseCase;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository.ProductoVendido;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarReportesService implements ConsultarReportesUseCase {

    private static final int LIMITE_MAXIMO = 50;

    private final ReporteVentasRepository reporteVentas;

    public ConsultarReportesService(ReporteVentasRepository reporteVentas) {
        this.reporteVentas = reporteVentas;
    }

    @Override
    public List<ProductoVendido> productosMasVendidos(int limite) {
        ReglaDeNegocioVioladaException.validar(limite >= 1 && limite <= LIMITE_MAXIMO, "INV-LIMITE",
                "El limite del ranking debe estar entre 1 y " + LIMITE_MAXIMO + ".");
        return reporteVentas.productosMasVendidos(limite);
    }
}
