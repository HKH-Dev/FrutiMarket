package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.LoteId;
import com.uniquindio.ecommerce.domain.valueobject.identidad.PuntoAcopioId;
import com.uniquindio.ecommerce.domain.valueobject.origenycalidad.TipoCultivo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoteRepositoryEnMemoria implements LoteRepository {

    private final Map<String, Lote> lotes = new HashMap<>();

    @Override
    public Lote almacenar(Lote lote) {
        lotes.put(lote.codigo(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> buscarPorId(LoteId id) {
        return Optional.ofNullable(lotes.get(id));
    }

    @Override
    public List<Lote> buscarTodos() {
        return List.copyOf(lotes.values());
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return lotes.values().stream()
                .anyMatch((Lote lote) -> lote.codigo().equals(codigo));
    }

    @Override
    public List<Lote> buscarDisponiblesPorProducto(Producto producto) {
        return lotes.values().stream()
                .filter((Lote lote) -> lote.producto().equals(producto))
                .filter(l -> l.estado ().admitePedidos())
                .filter(Lote::estaDisponible)
                .toList();
    }

    @Override
    public List<Lote> consultarPorCampesino(Campesino campesino) {
        return lotes.values().stream()
                .filter(l -> l.campesinoResponsable().equals(campesino))
                .toList();
    }

    @Override
    public List<Lote> buscarPorEstado(EstadoLote estado) {
        return lotes.values().stream()
                .filter(l -> l.estado() == estado)
                .toList();
    }

    @Override
    public List<Lote> buscarEnAcopioPorCultivo(PuntoAcopioId puntoAcopio, TipoCultivo tipoCultivo) {
        return lotes.values().stream()
                .filter(l -> l.estado() == EstadoLote.EN_ACOPIO)
                .filter(l -> l.puntoAcopioActual() != null && l.puntoAcopioActual().equals(puntoAcopio))
                .filter(l -> l.tipoCultivo() == tipoCultivo)
                .toList();
    }

    @Override
    public List<Lote> buscarVencidosNoCerrados() {
        LocalDate hoy = LocalDate.now();
        return lotes.values().stream()
                .filter(l -> l.estaVencido(hoy))
                .filter(l -> l.estado() != EstadoLote.VENCIDO)
                .toList();
    }

    @Override
    public void eliminar(Lote lote) {
        lotes.remove(lote.id());
    }
}
