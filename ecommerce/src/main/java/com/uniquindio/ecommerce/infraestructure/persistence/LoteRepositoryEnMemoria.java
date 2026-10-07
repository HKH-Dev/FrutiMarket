package com.uniquindio.ecommerce.infraestructure.persistence;

import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Producto;
import com.uniquindio.ecommerce.domain.entity.PuntoAcopio;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoteRepositoryEnMemoria implements LoteRepository {

    private final Map<String, Lote> lotes = new HashMap<>();

    @Override
    public Lote almacenar(Lote lote) {
        lotes.put(lote.getId(), lote);
        return lote;
    }

    @Override
    public Optional<Lote> buscarPorId(String id) {
        return Optional.ofNullable(lotes.get(id));
    }

    @Override
    public List<Lote> buscarTodos() {
        return List.copyOf(lotes.values());
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return lotes.values().stream()
                .anyMatch(l -> l.getCodigo().equals(codigo));
    }

    @Override
    public List<Lote> buscarDisponiblesPorProducto(Producto producto) {
        return lotes.values().stream()
                .filter(l -> l.getProducto().equals(producto))
                .filter(l -> l.getEstado().permiteVenta())
                .filter(Lote::tieneDisponibilidad)
                .toList();
    }

    @Override
    public List<Lote> consultarPorCampesino(Campesino campesino) {
        return lotes.values().stream()
                .filter(l -> l.getCampesino().equals(campesino))
                .toList();
    }

    @Override
    public List<Lote> buscarPorEstado(EstadoLote estado) {
        return lotes.values().stream()
                .filter(l -> l.getEstado() == estado)
                .toList();
    }

    @Override
    public List<Lote> buscarEnAcopioPorCultivo(PuntoAcopio puntoAcopio, TipoCultivo tipoCultivo) {
        return lotes.values().stream()
                .filter(l -> l.getEstado() == EstadoLote.EN_ACOPIO)
                .filter(l -> l.getPuntoAcopio() != null && l.getPuntoAcopio().equals(puntoAcopio))
                .filter(l -> l.getTipoCultivo() == tipoCultivo)
                .toList();
    }

    @Override
    public List<Lote> buscarVencidosNoCerrados() {
        LocalDate hoy = LocalDate.now();
        return lotes.values().stream()
                .filter(l -> l.estaVencido(hoy))
                .filter(l -> l.getEstado() != EstadoLote.CERRADO)
                .toList();
    }

    @Override
    public void eliminar(Lote lote) {
        lotes.remove(lote.getId());
    }
}
