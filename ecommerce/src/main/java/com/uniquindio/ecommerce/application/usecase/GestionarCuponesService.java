package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.application.port.in.GestionarCuponesUseCase;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.domain.comercial.Cupon;
import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import com.uniquindio.ecommerce.domain.repository.CompradorRepository;
import com.uniquindio.ecommerce.domain.repository.CuponRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CompradorId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class GestionarCuponesService implements GestionarCuponesUseCase {

    private final CuponRepository cupones;
    private final CompradorRepository compradores;
    private final Reloj reloj;

    public GestionarCuponesService(CuponRepository cupones, CompradorRepository compradores, Reloj reloj) {
        this.cupones = cupones;
        this.compradores = compradores;
        this.reloj = reloj;
    }

    @Override
    public Cupon crear(String codigo, CompradorId comprador, int porcentaje, LocalDate venceEl) {
        compradores.obtenerComprador(comprador).orElseThrow(() -> new RecursoNoEncontradoException("Comprador", comprador));
        Cupon cupon = Cupon.crear(codigo, comprador, porcentaje, venceEl, LocalDate.ofInstant(reloj.ahora(), ZoneId.systemDefault()));
        ReglaDeNegocioVioladaException.validar(cupones.porCodigo(cupon.codigo()).isEmpty(), "R24", "Ya existe un cupon con ese codigo.");
        cupones.guardar(cupon);
        return cupon;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cupon> todos() {
        return cupones.todos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cupon> delComprador(CompradorId comprador) {
        return cupones.delComprador(comprador);
    }
}
