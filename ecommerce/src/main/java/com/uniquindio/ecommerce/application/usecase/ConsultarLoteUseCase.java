package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.entity.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.entity.PuntoAcopio;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.EstadoLote;
import com.uniquindio.ecommerce.domain.valueobject.HitoDespacho;
import com.uniquindio.ecommerce.domain.valueobject.TipoCultivo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class ConsultarLoteUseCase implements LoteRepository {
    @Override
    public Lote almacenar(Lote lote) {
        return null;
    }

    @Override
    public Optional<Lote> buscarPorId(String id) {
        return Optional.empty();
    }

    @Override
    public List<Lote> buscarTodos() {
        return List.of();
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return false;
    }

    @Override
    public List<Lote> buscarDisponiblesPorProducto(Producto producto) {
        return List.of();
    }

    @Override
    public List<Lote> consultarPorCampesino(Campesino campesino) {
        return List.of();
    }

    @Override
    public List<Lote> buscarPorEstado(EstadoLote estado) {
        return List.of();
    }

    @Override
    public List<Lote> buscarEnAcopioPorCultivo(PuntoAcopio puntoAcopio, TipoCultivo tipoCultivo) {
        return List.of();
    }

    @Override
    public List<Lote> buscarVencidosNoCerrados() {
        return List.of();
    }

    @Override
    public void eliminar(Lote id) {

    }
}