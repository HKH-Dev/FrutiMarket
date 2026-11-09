package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.ConsultarPanelesUseCase;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Comprador;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.repository.PagoRepository;
import com.uniquindio.ecommerce.domain.repository.ReporteVentasRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarPanelesService implements ConsultarPanelesUseCase {

    private final PagoRepository pagos;
    private final CampesinoRepository campesinos;
    private final CompradorRepository compradores;
    private final ReporteVentasRepository ventas;

    public ConsultarPanelesService(PagoRepository pagos, CampesinoRepository campesinos, CompradorRepository compradores,
                                   ReporteVentasRepository ventas) {
        this.pagos = pagos;
        this.campesinos = campesinos;
        this.compradores = compradores;
        this.ventas = ventas;
    }

    @Override
    public PagoRepository.ResumenPagos resumenPagos() {
        return pagos.resumen();
    }

    @Override
    public List<Campesino> campesinos() {
        return campesinos.todos();
    }

    @Override
    public List<Comprador> compradores() {
        return compradores.todos();
    }

    @Override
    public List<ReporteVentasRepository.VentaDeCampesino> ventasDe(CampesinoId campesino) {
        return ventas.ventasDelCampesino(campesino);
    }
}
